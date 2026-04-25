package br.com.bolao.domain.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "scoring_config")
@Getter @Setter @NoArgsConstructor
public class ScoringConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "config_key", nullable = false, unique = true, length = 60)
    private String configKey;

    @Column(nullable = false)
    private int points;

    @Column(nullable = false, length = 200)
    private String description;
}
