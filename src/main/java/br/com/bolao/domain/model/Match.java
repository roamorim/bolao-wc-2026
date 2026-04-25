package br.com.bolao.domain.model;

import br.com.bolao.domain.enums.MatchStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "matches")
@Getter @Setter @NoArgsConstructor
public class Match {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "stage_id")
    private TournamentStage stage;

    @Column(name = "match_number", nullable = false)
    private int matchNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "home_team_id")
    private Team homeTeam;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "away_team_id")
    private Team awayTeam;

    @Column(name = "match_datetime", nullable = false)
    private Instant matchDatetime;

    @Column(name = "prediction_deadline", nullable = false)
    private Instant predictionDeadline;

    @Column(length = 200)
    private String venue;

    @Column(name = "home_score")
    private Integer homeScore;

    @Column(name = "away_score")
    private Integer awayScore;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MatchStatus status = MatchStatus.SCHEDULED;

    public boolean isOpen() {
        return status == MatchStatus.SCHEDULED && Instant.now().isBefore(predictionDeadline);
    }

    public boolean isFinished() {
        return status == MatchStatus.FINISHED;
    }
}
