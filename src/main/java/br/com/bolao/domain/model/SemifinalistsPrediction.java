package br.com.bolao.domain.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "semifinalists_prediction")
@Getter @Setter @NoArgsConstructor
public class SemifinalistsPrediction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", unique = true)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "team1_id")
    private Team team1;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "team2_id")
    private Team team2;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "team3_id")
    private Team team3;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "team4_id")
    private Team team4;

    @Column(name = "submitted_at", nullable = false)
    private Instant submittedAt = Instant.now();

    @Column(name = "points_earned")
    private Integer pointsEarned;

    @Column(nullable = false)
    private Instant deadline;
}
