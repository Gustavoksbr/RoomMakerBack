package com.example.roommaker.app.controllers.http.filters;

import com.example.roommaker.app.domain.exceptions.ErroDeAutenticacaoGeral;
import com.example.roommaker.app.domain.managers.usuario.UsuarioManager;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Optional;

@Component
public class JwtHTTPInterceptor implements HandlerInterceptor {
    @Autowired
    UsuarioManager usuarioManager;


    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
            String autorization = Optional.ofNullable(request.getHeader("Authorization"))
                    .orElseThrow(() -> new ErroDeAutenticacaoGeral("Usuário não autenticado"));
            String username = usuarioManager.capturarUsernameDoToken(autorization);
            request.setAttribute("username", username);
        return true;
    }
}