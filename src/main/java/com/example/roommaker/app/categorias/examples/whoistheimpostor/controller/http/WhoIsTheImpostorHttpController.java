package com.example.roommaker.app.categorias.examples.whoistheimpostor.controller.http;

import com.example.roommaker.app.categorias.examples.whoistheimpostor.controller.requests.VotoRequest;
import com.example.roommaker.app.categorias.examples.whoistheimpostor.domain.WhoIsTheImpostorManager;
import com.example.roommaker.app.categorias.examples.whoistheimpostor.domain.models.WhoIsTheImpostorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/categorias/whoistheimpostor")

// a escolha de usar http em vez de websocket eh pq http da retorno a quem fez a requisicao
// por ora so implementado um metodo
public class WhoIsTheImpostorHttpController {
    private final WhoIsTheImpostorManager whoIsTheImpostorManager;
    @Autowired
    public WhoIsTheImpostorHttpController(
            WhoIsTheImpostorManager whoIsTheImpostorManager
    ) {
        this.whoIsTheImpostorManager = whoIsTheImpostorManager;
    }


//    @PostMapping("/{usernameDono}/{nomeSala}/comecar")
//    public ResponseEntity<Void> comecar(HttpServletRequest request, @PathVariable String usernameDono, @PathVariable String nomeSala) {
//        String username = request.getAttribute("username").toString();
//        whoIsTheImpostorManager.comecarPartida(nomeSala,usernameDono,username);
//        return ResponseEntity.status(HttpStatus.CREATED).build();
//    }
//    @DeleteMapping("/{usernameDono}/{nomeSala}/terminar")
//    public ResponseEntity<Void> terminar(HttpServletRequest request, @PathVariable String usernameDono, @PathVariable String nomeSala) {
//        String username = request.getAttribute("username").toString();
//        whoIsTheImpostorManager.terminarPartida(nomeSala,usernameDono,username);
//        return ResponseEntity.noContent().build();
//    }
    @GetMapping("/{usernameDono}/{nomeSala}/mostrar")
    public ResponseEntity<WhoIsTheImpostorResponse> mostrar(HttpServletRequest request, @PathVariable String usernameDono, @PathVariable String nomeSala) {
        String username = request.getAttribute("username").toString();
       WhoIsTheImpostorResponse whoIsTheImpostorResponse = whoIsTheImpostorManager.mostrarJogoAtual(nomeSala,usernameDono,username);
        return ResponseEntity.ok(whoIsTheImpostorResponse);
    }
//    @PostMapping("/{usernameDono}/{nomeSala}/votar")
//    public ResponseEntity<Void> votar(HttpServletRequest request, @PathVariable String usernameDono, @PathVariable String nomeSala, @RequestBody @Valid  VotoRequest votoRequest) {
//        String username = request.getAttribute("username").toString();
//        whoIsTheImpostorManager.votar(nomeSala,usernameDono,username,votoRequest.getVoto());
//        return ResponseEntity.ok().build();
//    }
//    @DeleteMapping("/{usernameDono}/{nomeSala}/cancelarvoto")
//    public ResponseEntity<Void> cancelarVoto(HttpServletRequest request, @PathVariable String usernameDono, @PathVariable String nomeSala) {
//        String username = request.getAttribute("username").toString();
//        whoIsTheImpostorManager.cancelarVoto(nomeSala,usernameDono,username);
//        return ResponseEntity.noContent().build();
//    }
}
