package br.com.bolao.web.controller;

import br.com.bolao.domain.model.PasswordResetToken;
import br.com.bolao.service.PasswordResetService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Controller
public class ForgotPasswordController {

    private final PasswordResetService passwordResetService;

    public ForgotPasswordController(PasswordResetService passwordResetService) {
        this.passwordResetService = passwordResetService;
    }

    @GetMapping("/esqueci-senha")
    public String showForgotForm() {
        return "auth/forgot-password";
    }

    @PostMapping("/esqueci-senha")
    public String requestReset(@RequestParam String email, RedirectAttributes redirectAttributes) {
        passwordResetService.requestReset(email.trim().toLowerCase());
        redirectAttributes.addFlashAttribute("resetRequested",
                "Se o e-mail estiver cadastrado, você receberá as instruções em breve.");
        return "redirect:/login";
    }

    @GetMapping("/redefinir-senha")
    public String showResetForm(@RequestParam String token, Model model) {
        Optional<PasswordResetToken> resetToken = passwordResetService.findValidToken(token);
        if (resetToken.isEmpty()) {
            model.addAttribute("tokenError", "Link inválido ou expirado. Solicite um novo.");
        } else {
            model.addAttribute("token", token);
        }
        return "auth/reset-password";
    }

    @PostMapping("/redefinir-senha")
    public String resetPassword(
            @RequestParam String token,
            @RequestParam String newPassword,
            @RequestParam String confirmPassword,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (!newPassword.equals(confirmPassword)) {
            model.addAttribute("token", token);
            model.addAttribute("error", "As senhas não coincidem.");
            return "auth/reset-password";
        }

        if (newPassword.length() < 6) {
            model.addAttribute("token", token);
            model.addAttribute("error", "A senha deve ter pelo menos 6 caracteres.");
            return "auth/reset-password";
        }

        try {
            passwordResetService.resetPassword(token, newPassword);
            redirectAttributes.addFlashAttribute("success", "Senha redefinida com sucesso! Faça login.");
            return "redirect:/login";
        } catch (IllegalArgumentException e) {
            model.addAttribute("tokenError", e.getMessage());
            return "auth/reset-password";
        }
    }
}
