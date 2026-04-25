package br.com.bolao.domain.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "match_predictions")
@Getter @Setter @NoArgsConstructor
public class MatchPrediction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "match_id")
    private Match match;

    @Column(name = "home_score_pred", nullable = false)
    private int homeScorePred;

    @Column(name = "away_score_pred", nullable = false)
    private int awayScorePred;

    @Column(name = "submitted_at", nullable = false)
    private Instant submittedAt = Instant.now();

    // null = not yet calculated; 0 = calculated but scored zero
    @Column(name = "points_earned")
    private Integer pointsEarned;
}
