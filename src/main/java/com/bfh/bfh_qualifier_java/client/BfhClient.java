package com.bfh.bfh_qualifier_java.client;

import com.bfh.bfh_qualifier_java.dto.FinalQueryPayload;
import com.bfh.bfh_qualifier_java.dto.GenerateWebhookRequest;
import com.bfh.bfh_qualifier_java.dto.GenerateWebhookResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
@RequiredArgsConstructor
public class BfhClient {

    private final RestTemplate rest;

    @Value("${bfh-endpoints.generate}")
    private String generateUrl;

    @Value("${bfh-endpoints.fallbackSubmit}")
    private String fallbackSubmitUrl;

    public GenerateWebhookResponse generateWebhook(GenerateWebhookRequest req) {
        HttpHeaders h = new HttpHeaders();
        h.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<GenerateWebhookRequest> entity = new HttpEntity<>(req, h);

        ResponseEntity<GenerateWebhookResponse> resp =
                rest.exchange(generateUrl, HttpMethod.POST, entity, GenerateWebhookResponse.class);

        if (!resp.getStatusCode().is2xxSuccessful() || resp.getBody() == null) {
            throw new IllegalStateException("Failed to generate webhook: " + resp.getStatusCode());
        }
        return resp.getBody();
    }

    public void submitFinalQuery(String submissionUrl, String accessToken, String sql) {
        String target = (submissionUrl != null && !submissionUrl.isBlank()) ? submissionUrl : fallbackSubmitUrl;

        HttpHeaders h = new HttpHeaders();
        h.add(HttpHeaders.AUTHORIZATION, accessToken); // no Bearer
        h.setContentType(MediaType.APPLICATION_JSON);

        FinalQueryPayload payload = new FinalQueryPayload(sql);
        HttpEntity<FinalQueryPayload> entity = new HttpEntity<>(payload, h);

        ResponseEntity<String> resp =
                rest.exchange(target, HttpMethod.POST, entity, String.class);

        if (!resp.getStatusCode().is2xxSuccessful()) {
            throw new IllegalStateException("Submit failed: " + resp.getStatusCode() + " body=" + resp.getBody());
        }
        log.info("Submission success: {}", resp.getBody());
    }
}
