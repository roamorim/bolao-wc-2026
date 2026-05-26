package br.com.bolao.service;

import br.com.bolao.domain.model.*;
import java.util.List;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;

    @Value("${mail.enabled:false}")
    private boolean enabled;

    @Value("${spring.mail.username:}")
    private String from;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Async
    public void sendGroupStageSummaryConfirmation(User user, List<MatchPrediction> predictions) {
        if (!enabled) return;
        StringBuilder rows = new StringBuilder();
        predictions.forEach(p -> rows.append(
            "<tr><td style='padding:4px 8px'>" + p.getMatch().getHomeTeam().getName() + "</td>" +
            "<td style='padding:4px 8px;text-align:center'><strong>" +
            p.getHomeScorePred() + " x " + p.getAwayScorePred() +
            "</strong></td>" +
            "<td style='padding:4px 8px'>" + p.getMatch().getAwayTeam().getName() + "</td></tr>"));
        String body = body(user.getDisplayName(),
            "<p>Você preencheu todos os palpites da <strong>Fase de Grupos</strong>! Resumo:</p>" +
            "<table style='width:100%;border-collapse:collapse;font-size:0.95rem'>" + rows + "</table>");
        send(user.getEmail(), "✅ Palpites da fase de grupos completos!", body);
    }

    @Async
    public void sendBracketSummaryConfirmation(User user, List<BracketPick> picks) {
        if (!enabled) return;
        StringBuilder rows = new StringBuilder();
        picks.forEach(p -> rows.append(
            "<tr><td style='padding:4px 8px;color:#6c757d'>" + p.getMatch().getStage().getName() + "</td>" +
            "<td style='padding:4px 8px'>" + p.getMatch().getHomeTeam().getName() +
            " x " + p.getMatch().getAwayTeam().getName() + "</td>" +
            "<td style='padding:4px 8px'><strong>" + p.getPredictedWinner().getName() + "</strong></td></tr>"));
        String body = body(user.getDisplayName(),
            "<p>Você preencheu todos os palpites do <strong>Mata-mata</strong>! Resumo:</p>" +
            "<table style='width:100%;border-collapse:collapse;font-size:0.95rem'>" + rows + "</table>");
        send(user.getEmail(), "✅ Palpites do mata-mata completos!", body);
    }

    @Async
    public void sendGroupClassificationConfirmation(User user, String groupName,
            Team first, Team second, Team third, boolean thirdQualifies) {
        if (!enabled) return;
        String subject = "✅ Palpite salvo — Classificação Grupo " + groupName;
        String thirdLine = third != null
            ? "<li>3º lugar: <strong>" + third.getName() + "</strong>" +
              (thirdQualifies ? " (avança como melhor 3º)" : "") + "</li>"
            : "";
        String body = body(user.getDisplayName(),
            "<p>Seu palpite para o <strong>Grupo " + groupName + "</strong> foi salvo:</p>" +
            "<ul style='font-size:1.1rem'>" +
            "<li>1º lugar: <strong>" + first.getName() + "</strong></li>" +
            "<li>2º lugar: <strong>" + second.getName() + "</strong></li>" +
            thirdLine +
            "</ul>");
        send(user.getEmail(), subject, body);
    }

    @Async
    public void sendTopScorerConfirmation(User user, String playerName, Team team) {
        if (!enabled) return;
        String subject = "✅ Palpite salvo — Artilheiro";
        String body = body(user.getDisplayName(),
            "<p>Seu palpite de artilheiro foi salvo:</p>" +
            "<p style='font-size:1.5rem;text-align:center'>" +
            "<strong>" + playerName + "</strong><br/>" +
            "<span style='color:#6c757d'>" + team.getName() + "</span></p>");
        send(user.getEmail(), subject, body);
    }

    @Async
    public void sendBracketPickConfirmation(User user, Match match, Team winner) {
        if (!enabled) return;
        String home = match.getHomeTeam().getName();
        String away = match.getAwayTeam().getName();
        String subject = "✅ Palpite salvo — " + match.getStage().getName() + ": " + home + " x " + away;
        String body = body(user.getDisplayName(),
            "<p>Seu palpite de mata-mata foi salvo:</p>" +
            "<p style='font-size:1.1rem;text-align:center'>" + home + " x " + away + "</p>" +
            "<p style='font-size:1.5rem;text-align:center'>Vencedor: <strong>" + winner.getName() + "</strong></p>");
        send(user.getEmail(), subject, body);
    }

    private void send(String to, String subject, String htmlBody) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(new InternetAddress(from, "Bolão Copa 2026"));
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);
            mailSender.send(message);
            log.debug("Email enviado para {}: {}", to, subject);
        } catch (Exception e) {
            log.error("Falha ao enviar email para {}: {}", to, e.getMessage());
        }
    }

    private String body(String displayName, String content) {
        return """
            <div style="font-family:sans-serif;max-width:500px;margin:0 auto;padding:24px">
              <div style="background:#198754;padding:16px;border-radius:8px 8px 0 0;text-align:center">
                <h2 style="color:#fff;margin:0">⚽ Bolão Copa 2026</h2>
              </div>
              <div style="border:1px solid #dee2e6;border-top:0;padding:24px;border-radius:0 0 8px 8px">
                <p>Olá, <strong>%s</strong>!</p>
                %s
                <hr style="border:none;border-top:1px solid #dee2e6;margin:24px 0"/>
                <p style="color:#6c757d;font-size:0.85rem;text-align:center">
                  Bolão Copa do Mundo 2026 — Câmara dos Lordes
                </p>
              </div>
            </div>
            """.formatted(displayName, content);
    }
}
