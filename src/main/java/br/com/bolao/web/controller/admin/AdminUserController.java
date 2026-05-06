package br.com.bolao.web.controller.admin;

import br.com.bolao.service.UserService;
import br.com.bolao.web.dto.request.CreateUserRequest;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/users")
public class AdminUserController {

    private final UserService userService;

    public AdminUserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public String listUsers(Model model) {
        model.addAttribute("users", userService.findAll());
        model.addAttribute("createRequest", new CreateUserRequest());
        return "admin/users";
    }

    @PostMapping
    public String createUser(
            @Valid @ModelAttribute("createRequest") CreateUserRequest request,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Model model) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("users", userService.findAll());
            return "admin/users";
        }

        try {
            userService.createUser(request);
            redirectAttributes.addFlashAttribute("successMessage", "Usuário " + request.getUsername() + " criado.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/users";
    }

    @PostMapping("/{id}/toggle-active")
    public String toggleActive(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        userService.toggleActive(id);
        redirectAttributes.addFlashAttribute("successMessage", "Status do usuário atualizado.");
        return "redirect:/admin/users";
    }

    @PostMapping("/{id}/reset-password")
    public String resetPassword(
            @PathVariable Long id,
            @RequestParam String newPassword,
            RedirectAttributes redirectAttributes) {
        userService.resetPassword(id, newPassword);
        redirectAttributes.addFlashAttribute("successMessage", "Senha redefinida.");
        return "redirect:/admin/users";
    }

    @PostMapping("/{id}/edit")
    public String editUser(
            @PathVariable Long id,
            @RequestParam String displayName,
            @RequestParam String email,
            @RequestParam(defaultValue = "false") boolean admin,
            RedirectAttributes redirectAttributes) {
        try {
            userService.updateUser(id, displayName, email, admin);
            redirectAttributes.addFlashAttribute("successMessage", "Usuário atualizado.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/users";
    }

    @PostMapping("/{id}/delete")
    public String deleteUser(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails principal,
            RedirectAttributes redirectAttributes) {
        var user = userService.findById(id);
        if (user.getUsername().equals(principal.getUsername())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Você não pode excluir sua própria conta.");
            return "redirect:/admin/users";
        }
        userService.deleteUser(id);
        redirectAttributes.addFlashAttribute("successMessage", "Usuário " + user.getUsername() + " excluído.");
        return "redirect:/admin/users";
    }
}
