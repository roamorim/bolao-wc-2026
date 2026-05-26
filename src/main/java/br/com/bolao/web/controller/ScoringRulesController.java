package br.com.bolao.web.controller;

import br.com.bolao.domain.enums.ScoringKey;
import br.com.bolao.domain.repository.ScoringConfigRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/pontuacao")
public class ScoringRulesController {

    private final ScoringConfigRepository scoringConfigRepository;

    public ScoringRulesController(ScoringConfigRepository scoringConfigRepository) {
        this.scoringConfigRepository = scoringConfigRepository;
    }

    @GetMapping
    public String scoringRules(Model model) {
        Map<String, Integer> pts = scoringConfigRepository.findAll().stream()
            .collect(Collectors.toMap(c -> c.getConfigKey(), c -> c.getPoints()));

        model.addAttribute("groupExact",          pts.getOrDefault(ScoringKey.GROUP_EXACT_SCORE.name(), 0));
        model.addAttribute("groupWinnerDiff",     pts.getOrDefault(ScoringKey.GROUP_CORRECT_WINNER_AND_DIFF.name(), 0));
        model.addAttribute("groupWinner",         pts.getOrDefault(ScoringKey.GROUP_CORRECT_WINNER.name(), 0));
        model.addAttribute("groupDraw",           pts.getOrDefault(ScoringKey.GROUP_CORRECT_DRAW.name(), 0));
        model.addAttribute("classCorrectPos",     pts.getOrDefault(ScoringKey.GROUP_CLASSIFICATION_CORRECT_POSITION.name(), 0));
        model.addAttribute("classWrongPos",       pts.getOrDefault(ScoringKey.GROUP_CLASSIFICATION_WRONG_POSITION.name(), 0));
        model.addAttribute("thirdQualifies",      pts.getOrDefault(ScoringKey.GROUP_THIRD_QUALIFIES.name(), 0));
        model.addAttribute("bracketPick",         pts.getOrDefault(ScoringKey.BRACKET_CORRECT_PICK.name(), 0));
        model.addAttribute("topScorer",           pts.getOrDefault(ScoringKey.TOP_SCORER_CORRECT.name(), 0));

        return "scoring-rules";
    }
}
