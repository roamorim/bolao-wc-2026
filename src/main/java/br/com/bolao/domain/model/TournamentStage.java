package br.com.bolao.domain.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "tournament_stages")
@Getter @Setter @NoArgsConstructor
public class TournamentStage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 30)
    private String code;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(name = "display_order", nullable = false)
    private int displayOrder;

    @Column(name = "scored_on_ninety_minutes", nullable = false)
    private boolean scoredOnNinetyMinutes = true;

    public boolean isGroupStage() {
        return "GROUP".equals(code);
    }
}
