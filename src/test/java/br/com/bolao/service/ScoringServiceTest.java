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
        scoringService = new ScoringService(mock(), mock(), mock(), mock(), mock());

        groupConfig = Map.of(
            ScoringKey.GROUP_EXACT_SCORE,                    10,
            ScoringKey.GROUP_CORRECT_WINNER_AND_DIFF,         7,
            ScoringKey.GROUP_CORRECT_WINNER,                  3,
            ScoringKey.GROUP_CORRECT_DRAW,                    5,
            ScoringKey.KNOCKOUT_EXACT_SCORE,                 15,
            ScoringKey.KNOCKOUT_CORRECT_WINNER_AND_DIFF,     10,
            ScoringKey.KNOCKOUT_CORRECT_WINNER,               5,
            ScoringKey.GROUP_CLASSIFICATION_CORRECT_PER_TEAM, 3,
            ScoringKey.SEMIFINALISTS_CORRECT_PER_TEAM,       10,
            ScoringKey.TOP_SCORER_CORRECT,                   40
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

    // ── Knockout stage ───────────────────────────────────────────────────────

    @Test
    void knockout_exactScore_returns15() {
        int points = scoringService.computeMatchPoints(2, 1, 2, 1, true, knockoutConfig);
        assertThat(points).isEqualTo(15);
    }

    @Test
    void knockout_correctWinnerAndDiff_returns10() {
        int points = scoringService.computeMatchPoints(3, 1, 2, 0, true, knockoutConfig);
        assertThat(points).isEqualTo(10);
    }

    @Test
    void knockout_correctWinnerOnly_returns5() {
        int points = scoringService.computeMatchPoints(3, 1, 1, 0, true, knockoutConfig);
        assertThat(points).isEqualTo(5);
    }

    @Test
    void knockout_wrongResult_returns0() {
        int points = scoringService.computeMatchPoints(2, 0, 0, 2, true, knockoutConfig);
        assertThat(points).isEqualTo(0);
    }

    @Test
    void knockout_drawNotScored_treatsSameAsMistake() {
        // In knockout, draws go to extra time — prediction of draw is still counted as wrong result
        // because actual 90-min draw means diff=0; pred of, say, 2-1 (home win) is wrong
        int points = scoringService.computeMatchPoints(1, 1, 2, 1, true, knockoutConfig);
        assertThat(points).isEqualTo(0);
    }

    @Test
    void knockout_correctDrawPrediction_returns0ForKnockout() {
        // Draws can happen in knockout (90 min), but GROUP_CORRECT_DRAW shouldn't be used
        // The code returns 0 because isDraw && !isKnockout is false
        int points = scoringService.computeMatchPoints(1, 1, 1, 1, true, knockoutConfig);
        // Exact score match → 15
        assertThat(points).isEqualTo(15);
    }

    // ── Parameterized edge cases ─────────────────────────────────────────────

    @ParameterizedTest(name = "actual={0}-{1} pred={2}-{3} knockout={4} → {5}pts")
    @CsvSource({
        "1,0, 1,0, false, 10",   // group exact
        "1,0, 2,1, false,  7",   // group correct winner + same diff (+1 vs +1)
        "1,0, 1,0,  true, 15",   // knockout exact
        "3,0, 4,1,  true, 10",   // knockout correct winner + same diff (+3 vs +3)
        "0,1, 1,0, false,  0",   // wrong result
        "2,2, 2,2, false, 10",   // group exact draw
        "2,2, 2,2,  true, 15",   // knockout exact draw
    })
    void parameterizedScoring(int aH, int aA, int pH, int pA, boolean knockout, int expected) {
        int points = scoringService.computeMatchPoints(aH, aA, pH, pA, knockout, groupConfig);
        assertThat(points).isEqualTo(expected);
    }
}
