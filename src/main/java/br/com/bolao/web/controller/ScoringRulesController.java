package br.com.bolao.web.controller;

import br.com.bolao.domain.enums.ScoringKey;
import br.com.bolao.domain.model.User;
import br.com.bolao.domain.repository.ScoringConfigRepository;
import br.com.bolao.domain.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/pontuacao")
public class ScoringRulesController {

    private final ScoringConfigRepository scoringConfigRepository;
    private final UserRepository userRepository;

    @Value("${bolao.payment.amount.value:150}")
    private long paymentAmountValue;

    @Value("${bolao.currency.symbol:R$}")
    private String currencySymbol;

    @Value("${bolao.hosting.cost:0}")
    private long hostingCost;

    public ScoringRulesController(ScoringConfigRepository scoringConfigRepository, UserRepository userRepository) {
        this.scoringConfigRepository = scoringConfigRepository;
        this.userRepository = userRepository;
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

        long participantCount = userRepository.findAll().stream()
            .filter(u -> u.isActive() && u.getRole().name().equals("USER"))
            .count();

        long gross = participantCount * paymentAmountValue;
        long net = Math.max(0, gross - hostingCost);
        long prize1 = net * 70 / 100;
        long prize2 = net * 20 / 100;
        long prize3 = net * 10 / 100;

        NumberFormat nf = NumberFormat.getIntegerInstance(new Locale("pt", "BR"));

        model.addAttribute("participantCount",  participantCount);
        model.addAttribute("paymentAmountFmt",  nf.format(paymentAmountValue));
        model.addAttribute("grossFmt",          nf.format(gross));
        model.addAttribute("netFmt",            nf.format(net));
        model.addAttribute("hostingCostFmt",    nf.format(hostingCost));
        model.addAttribute("prize1Fmt",         nf.format(prize1));
        model.addAttribute("prize2Fmt",         nf.format(prize2));
        model.addAttribute("prize3Fmt",         nf.format(prize3));
        model.addAttribute("currencySymbol",    currencySymbol);
        model.addAttribute("hostingCost",       hostingCost);

        return "scoring-rules";
    }
}
