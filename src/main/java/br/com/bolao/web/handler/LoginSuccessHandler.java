package br.com.bolao.web.handler;

import br.com.bolao.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final UserService userService;

    public LoginSuccessHandler(UserService userService) {
        this.userService = userService;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        var user = userService.findByUsername(authentication.getName());
        if (user.isMustChangePassword()) {
            response.sendRedirect(request.getContextPath() + "/alterar-senha");
        } else {
            response.sendRedirect(request.getContextPath() + "/dashboard");
        }
    }
}
