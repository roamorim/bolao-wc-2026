package br.com.bolao.web.controller;

import br.com.bolao.service.UserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/alterar-senha")
public class ChangePasswordController {

    private final UserService userService;

    public ChangePasswordController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public String form() {
        return "auth/change-password";
    }

    @PostMapping
    public String submit(
            @AuthenticationPrincipal UserDetails principal,
            @RequestParam String currentPassword,
            @RequestParam String newPassword,
            @RequestParam String confirmPassword,
            RedirectAttributes redirectAttributes) {

        if (!newPassword.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("errorMessage", "A nova senha e a confirmação não coincidem.");
            return "redirect:/alterar-senha";
        }
        if (newPassword.length() < 6) {
            redirectAttributes.addFlashAttribute("errorMessage", "A nova senha deve ter pelo menos 6 caracteres.");
            return "redirect:/alterar-senha";
        }
        if (newPassword.equals(currentPassword)) {
            redirectAttributes.addFlashAttribute("errorMessage", "A nova senha deve ser diferente da senha atual.");
            return "redirect:/alterar-senha";
        }

        try {
            userService.changePassword(principal.getUsername(), currentPassword, newPassword);
            redirectAttributes.addFlashAttribute("successMessage", "Senha alterada com sucesso.");
            return "redirect:/dashboard";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/alterar-senha";
        }
    }
}
