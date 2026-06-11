package br.com.bolao.service;

import br.com.bolao.domain.model.PasswordResetToken;
import br.com.bolao.domain.model.User;
import br.com.bolao.domain.repository.PasswordResetTokenRepository;
import br.com.bolao.domain.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Optional;

@Service
public class PasswordResetService {

    private static final Logger log = LoggerFactory.getLogger(PasswordResetService.class);
    private static final int TOKEN_EXPIRY_HOURS = 1;

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final String baseUrl;

    public PasswordResetService(
            UserRepository userRepository,
            PasswordResetTokenRepository tokenRepository,
            PasswordEncoder passwordEncoder,
            EmailService emailService,
            @Value("${bolao.base-url:http://localhost:8080}") String baseUrl) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
        this.baseUrl = baseUrl;
    }

    @Transactional
    public void requestReset(String email) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            // Don't reveal whether the email is registered
            log.debug("Password reset requested for unknown email: {}", email);
            return;
        }
        User user = userOpt.get();

        tokenRepository.deleteByUser(user);

        String raw = generateToken();
        PasswordResetToken token = new PasswordResetToken();
        token.setUser(user);
        token.setToken(raw);
        token.setExpiresAt(Instant.now().plus(TOKEN_EXPIRY_HOURS, ChronoUnit.HOURS));
        tokenRepository.save(token);

        String resetLink = baseUrl + "/redefinir-senha?token=" + raw;
        emailService.sendPasswordResetEmail(user.getDisplayName(), user.getEmail(), resetLink);
        log.info("Password reset token generated for user: {}", user.getUsername());
    }

    public Optional<PasswordResetToken> findValidToken(String token) {
        return tokenRepository.findByToken(token).filter(PasswordResetToken::isValid);
    }

    @Transactional
    public void resetPassword(String rawToken, String newPassword) {
        PasswordResetToken token = tokenRepository.findByToken(rawToken)
                .orElseThrow(() -> new IllegalArgumentException("Link inválido ou expirado."));

        if (!token.isValid()) {
            throw new IllegalArgumentException("Link inválido ou expirado.");
        }

        User user = token.getUser();
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        user.setMustChangePassword(false);
        token.setUsed(true);
        log.info("Password reset completed for user: {}", user.getUsername());
    }

    private String generateToken() {
        byte[] bytes = new byte[32];
        new SecureRandom().nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
