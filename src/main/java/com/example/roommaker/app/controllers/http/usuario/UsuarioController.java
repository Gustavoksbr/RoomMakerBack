package com.example.roommaker.app.controllers.http.usuario;


import com.example.roommaker.app.controllers.http.usuario.request.*;
import com.example.roommaker.app.domain.models.JwtResponse;
import com.example.roommaker.app.domain.models.Response;
import com.example.roommaker.app.controllers.http.usuario.response.UsuarioParaListarResponse;
import com.example.roommaker.app.domain.models.Usuario;
import com.example.roommaker.app.domain.managers.usuario.UsuarioManager;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class UsuarioController {
   @Autowired
   private UsuarioManager usuarioManager;



    @PostMapping("/login")
    public ResponseEntity<Response> authenticate(@RequestBody @Valid UsuarioParaEntrarRequest usuario){
//        System.out.println("tentando logar "+usuario.toString());
     Response response = usuarioManager.authenticate(usuario.toDomain());
     return ResponseEntity.ok(response);
}

 @PostMapping("/cadastro")
public ResponseEntity<JwtResponse> createUser(@RequestBody @Valid UsuarioParaCriarRequest usuario) throws InterruptedException {
    JwtResponse jwtResponse = new JwtResponse(usuarioManager.createUser(usuario.toDomain()));
    return ResponseEntity.ok(jwtResponse);
}
    @PostMapping("/usuario/esquecisenha")
    public ResponseEntity<Void> esqueciSenha(@RequestBody EsqueciSenhaRequest esqueciSenhaRequest){
        Usuario usuario = Usuario.builder().email(esqueciSenhaRequest.getEmail()).build();
        usuarioManager.esqueciSenha(usuario);
        return ResponseEntity.ok().build();

    }
    @PostMapping("/usuario/novasenha")
    public ResponseEntity<JwtResponse> novaSenha(@RequestBody CodigoRecuperacaoSenhaRequest codigoPorEmailRequest){
        Usuario usuario = Usuario.builder().email(codigoPorEmailRequest.getEmail()).password(codigoPorEmailRequest.getPassword()).build();
        JwtResponse jwtResponse = new JwtResponse(usuarioManager.alterarSenha(usuario, codigoPorEmailRequest.getCodigo()));
        return ResponseEntity.ok(jwtResponse);
    }
// endpoints prontos no back end mas não implementados no front end:

@PostMapping("/login2fa")
public ResponseEntity<JwtResponse> authenticate2fa(@RequestBody @Valid CodigoPorEmailRequest codigoPorEmailRequest){
    Usuario usuario = Usuario.builder().email(codigoPorEmailRequest.getEmail()).build();
    JwtResponse jwtResponse = new JwtResponse(usuarioManager.authenticate2fa(usuario, codigoPorEmailRequest.getCodigo()));
    return ResponseEntity.ok(jwtResponse);
}
@PutMapping("/usuario/doisfatores")
public ResponseEntity<Boolean> doisFatores(HttpServletRequest request) {
    String token = request.getHeader("Authorization");
    return ResponseEntity.ok(usuarioManager.habilitarDesabilitarDoisFatores(token));
}

@GetMapping("/usuarios")
public ResponseEntity<List<UsuarioParaListarResponse>> listarUsuarios(@RequestParam(defaultValue = "") String substring){
    List<UsuarioParaListarResponse> lista;
    lista = usuarioManager.listarUsuarios(substring).stream().map(UsuarioParaListarResponse::new).toList();
    return ResponseEntity.ok(lista);
}

    @GetMapping("/datanascimento")
    public ResponseEntity<Map<String, String>> getDataNascimento(HttpServletRequest request) {
        String username = request.getAttribute("username").toString();
        LocalDate dataNascimento = usuarioManager.getDataNascimento(username);

        Map<String, String> response = new HashMap<>();
        response.put("data", dataNascimento != null ? dataNascimento.toString() : "");

        return ResponseEntity.ok(response);
    }


// endpoints incompletos. São possíveis ideias no futuro:

//@PutMapping("profile")

//   @GetMapping("/username")
//   public ResponseEntity<Username> getUsername(HttpServletRequest request){
//       String username = request.getAttribute("username").toString();
//       Username usernameClass = new Username(username);
//       return ResponseEntity.ok(usernameClass);
//   }
   }

