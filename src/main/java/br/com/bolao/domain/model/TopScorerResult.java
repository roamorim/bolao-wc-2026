package br.com.bolao.domain.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "top_scorer_result")
@Getter @Setter @NoArgsConstructor
public class TopScorerResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String playerName;

    @Column(nullable = false)
    private Instant recordedAt = Instant.now();
}
