package com.example.roommaker.app.controllers.http.filters;

import com.example.roommaker.app.domain.exceptions.ErroDeAutenticacaoGeral;
import com.example.roommaker.app.domain.ports.auth.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Optional;

/**
 * Interceptor de autenticação JWT.
 *
 * Valida apenas a assinatura do token (operação local, sem I/O).
 * A verificação de existência do usuário no banco foi removida pois o JWT
 * autocontido já garante autenticidade via HMAC-SHA256.
 */
@Component
public class JwtHTTPInterceptor implements HandlerInterceptor {

    private final JwtService jwtService;

    public JwtHTTPInterceptor(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // Endpoints públicos (modo sem login)
        if (isPublicEndpoint(request)) {
            return true;
        }

        String authorization = Optional.ofNullable(request.getHeader("Authorization"))
                .orElseThrow(() -> new ErroDeAutenticacaoGeral("Usuário não autenticado"));
        String username = jwtService.getUsername(authorization);
        request.setAttribute("username", username);
        return true;
    }

    private boolean isPublicEndpoint(HttpServletRequest request) {
        String method = request.getMethod();
        if (!"GET".equalsIgnoreCase(method)) return false;

        String uri = request.getRequestURI();
        if (uri == null) return false;

        // Lista/filtra salas: GET /salas?...
        if ("/salas".equals(uri) || "/salas/".equals(uri)) return true;

        // Visualizar a página "fora da sala": GET /salas/{usernameDono}/{nomeSala}
        return uri.matches("^/salas/[^/]+/[^/]+$");
    }
}