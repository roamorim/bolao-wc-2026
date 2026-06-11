package br.com.bolao.web.interceptor;

import br.com.bolao.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class PasswordChangeInterceptor implements HandlerInterceptor {

    private final UserService userService;

    public PasswordChangeInterceptor(UserService userService) {
        this.userService = userService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return true;
        }

        String path = request.getRequestURI();
        if (path.startsWith("/alterar-senha") || path.startsWith("/logout")
                || path.startsWith("/esqueci-senha") || path.startsWith("/redefinir-senha")
                || path.startsWith("/css") || path.startsWith("/js")
                || path.startsWith("/images") || path.startsWith("/favicon")) {
            return true;
        }

        var user = userService.findByUsername(auth.getName());
        if (user.isMustChangePassword()) {
            response.sendRedirect(request.getContextPath() + "/alterar-senha");
            return false;
        }
        return true;
    }
}
