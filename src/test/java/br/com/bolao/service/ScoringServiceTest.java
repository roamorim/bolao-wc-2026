package br.com.bolao.service;

import br.com.bolao.domain.enums.ScoringKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class ScoringServiceTest {

    private ScoringService scoringService;
    private Map<ScoringKey, Integer> groupConfig;
    private Map<ScoringKey, Integer> knockoutConfig;

    @BeforeEach
    void setUp() {
        scoringService = new ScoringService(mock(), mock(), mock(), mock(), mock(), mock());

        groupConfig = Map.of(
            ScoringKey.GROUP_EXACT_SCORE,                10,
            ScoringKey.GROUP_CORRECT_WINNER_AND_DIFF,     7,
            ScoringKey.GROUP_CORRECT_WINNER,              3,
            ScoringKey.GROUP_CORRECT_DRAW,                5,
            ScoringKey.SEMIFINALISTS_CORRECT_PER_TEAM,   10,
            ScoringKey.TOP_SCORER_CORRECT,               40
        );
        knockoutConfig = groupConfig;
    }

    // ── Group stage ──────────────────────────────────────────────────────────

    @Test
    void groupStage_exactScore_returns10() {
        int points = scoringService.computeMatchPoints(2, 1, 2, 1, false, groupConfig);
        assertThat(points).isEqualTo(10);
    }

    @Test
    void groupStage_correctWinnerAndDiff_returns7() {
        // actual 3-1 (diff +2), pred 2-0 (diff +2) — same diff, correct winner
        int points = scoringService.computeMatchPoints(3, 1, 2, 0, false, groupConfig);
        assertThat(points).isEqualTo(7);
    }

    @Test
    void groupStage_correctWinnerOnly_returns3() {
        // actual 3-1 (home wins), pred 1-0 (home wins, different diff)
        int points = scoringService.computeMatchPoints(3, 1, 1, 0, false, groupConfig);
        assertThat(points).isEqualTo(3);
    }

    @Test
    void groupStage_correctDraw_returns5() {
        int points = scoringService.computeMatchPoints(1, 1, 0, 0, false, groupConfig);
        assertThat(points).isEqualTo(5);
    }

    @Test
    void groupStage_wrongResult_returns0() {
        // actual home wins, pred away wins
        int points = scoringService.computeMatchPoints(2, 0, 0, 1, false, groupConfig);
        assertThat(points).isEqualTo(0);
    }

    @Test
    void groupStage_predictedDrawButActuallyHome_returns0() {
        int points = scoringService.computeMatchPoints(1, 0, 0, 0, false, groupConfig);
        assertThat(points).isEqualTo(0);
    }

    @Test
    void groupStage_predictedHomeButActuallyDraw_returns0() {
        int points = scoringService.computeMatchPoints(1, 1, 2, 0, false, groupConfig);
        assertThat(points).isEqualTo(0);
    }

    @Test
    void groupStage_exactScoreZeroZero_returns10() {
        int points = scoringService.computeMatchPoints(0, 0, 0, 0, false, groupConfig);
        assertThat(points).isEqualTo(10);
    }

    // ── Mata-mata ────────────────────────────────────────────────────────────

    @Test
    void knockout_sempreRetorna0_pontuacaoViabracketPick() {
        // Partidas do mata-mata não pontuam por placar — somente bracket picks
        assertThat(scoringService.computeMatchPoints(2, 1, 2, 1, true, knockoutConfig)).isEqualTo(0);
        assertThat(scoringService.computeMatchPoints(3, 1, 1, 0, true, knockoutConfig)).isEqualTo(0);
        assertThat(scoringService.computeMatchPoints(1, 1, 1, 1, true, knockoutConfig)).isEqualTo(0);
    }

    // ── Casos parametrizados ─────────────────────────────────────────────────

    @ParameterizedTest(name = "real={0}-{1} aposta={2}-{3} mata-mata={4} → {5}pts")
    @CsvSource({
        "1,0, 1,0, false, 10",   // placar exato grupos
        "1,0, 2,1, false,  7",   // vencedor + saldo correto grupos
        "0,1, 1,0, false,  0",   // resultado errado
        "2,2, 2,2, false, 10",   // empate exato grupos
        "1,0, 1,0,  true,  0",   // mata-mata → sempre 0
        "2,1, 2,1,  true,  0",   // mata-mata → sempre 0
    })
    void pontuacaoParametrizada(int aH, int aA, int pH, int pA, boolean knockout, int expected) {
        int points = scoringService.computeMatchPoints(aH, aA, pH, pA, knockout, groupConfig);
        assertThat(points).isEqualTo(expected);
    }
}
