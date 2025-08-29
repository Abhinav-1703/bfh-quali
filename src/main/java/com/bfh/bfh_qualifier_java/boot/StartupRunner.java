
package com.bfh.bfh_qualifier_java.boot;

import com.bfh.bfh_qualifier_java.client.BfhClient;
import com.bfh.bfh_qualifier_java.dto.GenerateWebhookRequest;
import com.bfh.bfh_qualifier_java.dto.GenerateWebhookResponse;
import com.bfh.bfh_qualifier_java.persist.Submission;
import com.bfh.bfh_qualifier_java.persist.SubmissionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.time.Instant;

@Slf4j
@Component
@RequiredArgsConstructor
public class StartupRunner implements ApplicationRunner {

    private final BfhClient client;
    private final SubmissionRepository repo;

    @Value("${bfh.name}")   private String name;
    @Value("${bfh.regNo}")  private String regNo;
    @Value("${bfh.email}")  private String email;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        log.info("Starting BFH Qualifier app for {} ({})", name, regNo);

        // 1. Call generateWebhook
        GenerateWebhookResponse response = client.generateWebhook(
                new GenerateWebhookRequest(name, regNo, email));

        String webhook = response.getWebhook();
        String token   = response.getAccessToken();

        // 2. Always Q1 for odd regNo (your case)
        String sqlPath = "sql/Q1.sql";
        String finalSql = new String(new ClassPathResource(sqlPath)
                .getInputStream().readAllBytes(), StandardCharsets.UTF_8).trim();

        if (finalSql.isBlank()) throw new IllegalStateException("SQL is empty");

        // 3. Save to local DB
        Submission saved = repo.save(Submission.builder()
                .regNo(regNo)
                .questionId("Q1")
                .finalQuery(finalSql)
                .webhookUrl(webhook)
                .tokenSuffix(token != null ? token.substring(Math.max(0, token.length()-6)) : null)
                .createdAt(Instant.now())
                .build());

        log.info("Saved submission locally with id={}", saved.getId());

        // 4. Submit SQL to webhook
        client.submitFinalQuery(webhook, token, finalSql);
        log.info("Done");
    }
}
