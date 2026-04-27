package br.com.bolao.domain.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "group_results")
@Getter @Setter @NoArgsConstructor
public class GroupResult {

    @Id
    @Column(name = "group_name", length = 1)
    private String groupName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "first_team_id")
    private Team firstTeam;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "second_team_id")
    private Team secondTeam;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "third_team_id")
    private Team thirdTeam;

    @Column(name = "third_qualifies", nullable = false)
    private boolean thirdQualifies = false;

    @Column(name = "recorded_at", nullable = false)
    private Instant recordedAt = Instant.now();
}
