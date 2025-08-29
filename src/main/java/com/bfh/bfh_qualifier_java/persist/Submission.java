package com.bfh.bfh_qualifier_java.persist;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class Submission {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String regNo;
    private String questionId;

    @Lob
    private String finalQuery;

    private String webhookUrl;
    private String tokenSuffix;
    private Instant createdAt;
}
