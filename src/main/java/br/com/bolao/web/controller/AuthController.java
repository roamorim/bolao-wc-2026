package br.com.bolao.web.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AuthController {

    @Value("${bolao.subtitle:Câmara dos Lordes}")
    private String subtitle;

    @Value("${bolao.payment.amount:R$ 150}")
    private String paymentAmount;

    @Value("${bolao.payment.key:c2297554-7770-4a72-8b6f-b5ee46d3473a}")
    private String paymentKey;

    @Value("${bolao.contact.url:https://wa.me/56963891924}")
    private String contactUrl;

    @Value("${bolao.contact.label:Falar com o Rodrigo}")
    private String contactLabel;

    @GetMapping("/login")
    public String loginPage(Model model) {
        model.addAttribute("subtitle", subtitle);
        model.addAttribute("paymentAmount", paymentAmount);
        model.addAttribute("paymentKey", paymentKey);
        model.addAttribute("contactUrl", contactUrl);
        model.addAttribute("contactLabel", contactLabel);
        return "auth/login";
    }

    @GetMapping("/")
    public String home() {
        return "redirect:/dashboard";
    }
}
