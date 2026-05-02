package com.example.roommaker.app.categorias.examples.xadrez.controller.http;

import com.example.roommaker.app.categorias.examples.xadrez.domain.XadrezManager;
import com.example.roommaker.app.categorias.examples.xadrez.domain.model.XadrezResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/categorias/xadrez")
public class XadrezHttpController {

    private final XadrezManager xadrezManager;

    @Autowired
    public XadrezHttpController(XadrezManager xadrezManager) {
        this.xadrezManager = xadrezManager;
    }

    /** Retorna o estado atual da sala de xadrez para o usuário autenticado. */
    @GetMapping("/{usernameDono}/{nomeSala}/mostrar")
    public ResponseEntity<XadrezResponse> mostrar(
            HttpServletRequest request,
            @PathVariable String usernameDono,
            @PathVariable String nomeSala) {
        String username = request.getAttribute("username").toString();
        XadrezResponse response = xadrezManager.mostrar(nomeSala, usernameDono, username);
        return ResponseEntity.ok(response);
    }
}
