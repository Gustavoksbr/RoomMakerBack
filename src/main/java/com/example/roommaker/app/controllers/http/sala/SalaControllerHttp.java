package com.example.roommaker.app.controllers.http.sala;

import com.example.roommaker.app.controllers.http.sala.dtos.EntrarSalaPorSenha;
import com.example.roommaker.app.controllers.http.sala.dtos.SalaRequest;
import com.example.roommaker.app.controllers.http.sala.dtos.SalaResponse;
import com.example.roommaker.app.domain.models.Sala;
import com.example.roommaker.app.domain.managers.sala.SalaManager;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/salas")
public class SalaControllerHttp {
    private final SalaManager salaManager;
   
    @Autowired
    public SalaControllerHttp(SalaManager salaManager) {
        this.salaManager = salaManager;
    }
    @GetMapping
    public ResponseEntity<List<SalaResponse>> listar(HttpServletRequest request,
                                                     @RequestParam(defaultValue = "") String usernameDono,
                                                     @RequestParam(defaultValue = "") String nome,
                                                     @RequestParam(defaultValue = "") String categoria) {
//        String autorization = Optional.ofNullable(request.getHeader("Authorization")).orElseThrow(() -> new ErroDeAutenticacaoGeral("Usuário não autenticado"));
//        this.capturarUsernameDoToken(autorization);
        List<SalaResponse> lista = this.salaManager.listar(usernameDono,nome,categoria).stream().map(SalaResponse::new).toList();
        return ResponseEntity.ok(lista);
    }
    @GetMapping("/_convidado") //lista as salas que o usuário é convidado
    public ResponseEntity<List<SalaResponse>>
    listarPorParticipante(HttpServletRequest request,
                          @RequestParam(defaultValue = "") String usernameParticipante){
        List<SalaResponse> lista = this.salaManager.listarPorParticipante(usernameParticipante).stream().map(SalaResponse::new).toList();
        return ResponseEntity.ok(lista);
    }
    @GetMapping("/_dono") //lista as salas que o usuário é dono
    public ResponseEntity<List<SalaResponse>>
    listarPorDono(HttpServletRequest request,
                  @RequestParam(defaultValue = "") String usernameDono){
        List<SalaResponse> lista = this.salaManager.listarPorDono(usernameDono).stream().map(SalaResponse::new).toList();
        return ResponseEntity.ok(lista);
    }

    // post url /salas body SalaRequest

    @PostMapping
    public ResponseEntity<SalaResponse> criar(@RequestBody @Valid SalaRequest sala, HttpServletRequest request) {
            String username = request.getAttribute("username").toString();
            Sala salaCriada = this.salaManager.criar(sala.toDomain(), username);
            SalaResponse salaResponse = new SalaResponse(salaCriada);
            return ResponseEntity.ok(salaResponse);
    }
    @PostMapping("/{usernameDono}/{nomeSala}")
    public ResponseEntity<SalaResponse> EntrarNaSala(@PathVariable String usernameDono, @PathVariable String nomeSala, HttpServletRequest request, @RequestBody @Valid EntrarSalaPorSenha senha)
    {

        String username = request.getAttribute("username").toString();
        Sala sala = this.salaManager.autenticarParticipante(nomeSala,usernameDono,senha.getSenha(), username );
        SalaResponse salaResponse = new SalaResponse(sala);
        return ResponseEntity.ok(salaResponse);
    }
    @GetMapping("/{usernameDono}/{nomeSala}")
    public ResponseEntity<SalaResponse> MostrarSala(@PathVariable String usernameDono, @PathVariable String nomeSala)
    {
        Sala sala = this.salaManager.mostrarSala(nomeSala,usernameDono);
        SalaResponse salaResponse = new SalaResponse(sala);
        return ResponseEntity.ok(salaResponse);
    }
    @DeleteMapping("/{usernameDono}/{nomeSala}")
    public ResponseEntity<Void> deletarSala(@PathVariable String usernameDono, @PathVariable String nomeSala, HttpServletRequest request)
    {
        String username = request.getAttribute("username").toString();
        this.salaManager.excluirSala(usernameDono,nomeSala,username);
        return ResponseEntity.noContent().build();
    }
    @DeleteMapping("/{usernameDono}/{nomeSala}/{usernameParticipante}")
    public ResponseEntity<SalaResponse> sairDaSala(@PathVariable String usernameDono, @PathVariable String nomeSala, @PathVariable String usernameParticipante, HttpServletRequest request)
    {
        String username = request.getAttribute("username").toString();
        Sala sala = this.salaManager.sairDaSala(usernameDono,nomeSala,usernameParticipante,username);
        return ResponseEntity.ok(new SalaResponse(sala));
    }
}