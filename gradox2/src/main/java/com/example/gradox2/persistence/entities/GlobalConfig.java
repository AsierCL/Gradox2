package com.example.gradox2.persistence.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "vote_config")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GlobalConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Integer quorumRequired;

    @Column(nullable = false)
    private Double approvalThreshold;

    @Column(nullable = false)
    private Integer maxPendingUploads;
}
