package br.com.bolao.domain.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "bracket_picks",
    uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "match_id"}))
@Getter @Setter @NoArgsConstructor
public class BracketPick {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "match_id", nullable = false)
    private Match match;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "predicted_winner_id")
    private Team predictedWinner;

    @Column(name = "points_earned")
    private Integer pointsEarned;

    @Column(name = "submitted_at")
    private Instant submittedAt;
}
