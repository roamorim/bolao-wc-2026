package br.com.bolao.service;

import br.com.bolao.domain.enums.MatchStatus;
import br.com.bolao.domain.model.Match;
import br.com.bolao.domain.model.ScoringConfig;
import br.com.bolao.domain.repository.MatchRepository;
import br.com.bolao.domain.repository.ScoringConfigRepository;
import br.com.bolao.web.dto.request.MatchResultRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TournamentAdminService {

    private final MatchRepository matchRepository;
    private final ScoringConfigRepository scoringConfigRepository;
    private final ScoringService scoringService;
    private final BracketAssemblyService bracketAssemblyService;

    public TournamentAdminService(
            MatchRepository matchRepository,
            ScoringConfigRepository scoringConfigRepository,
            ScoringService scoringService,
            BracketAssemblyService bracketAssemblyService) {
        this.matchRepository = matchRepository;
        this.scoringConfigRepository = scoringConfigRepository;
        this.scoringService = scoringService;
        this.bracketAssemblyService = bracketAssemblyService;
    }

    @Transactional
    public void submitMatchResult(Long matchId, MatchResultRequest request) {
        Match match = matchRepository.findById(matchId)
            .orElseThrow(() -> new IllegalArgumentException("Jogo não encontrado: " + matchId));

        if (match.getStatus() == MatchStatus.FINISHED) {
            throw new IllegalStateException("Resultado já lançado para o jogo " + matchId);
        }

        match.setHomeScore(request.homeScore());
        match.setAwayScore(request.awayScore());
        match.setStatus(MatchStatus.FINISHED);

        scoringService.calculateMatchPredictions(match);

        if (!match.getStage().isGroupStage()) {
            scoringService.calculateBracketPicks(match);
            bracketAssemblyService.advanceStageIfComplete(match.getStage().getCode());
        }
    }

    @Transactional
    public void lockMatch(Long matchId) {
        Match match = matchRepository.findById(matchId)
            .orElseThrow(() -> new IllegalArgumentException("Jogo não encontrado: " + matchId));

        if (match.getStatus() == MatchStatus.SCHEDULED) {
            match.setStatus(MatchStatus.LOCKED);
        }
    }

    @Transactional
    public void updateScoringConfig(Long configId, int newPoints) {
        ScoringConfig config = scoringConfigRepository.findById(configId)
            .orElseThrow(() -> new IllegalArgumentException("Configuração não encontrada: " + configId));
        config.setPoints(newPoints);
    }

    @Transactional
    public void recalculateAll() {
        List<Match> finished = matchRepository.findAll().stream()
            .filter(m -> m.getStatus() == MatchStatus.FINISHED)
            .toList();
        scoringService.recalculateAllMatchPredictions(finished);
    }
}
