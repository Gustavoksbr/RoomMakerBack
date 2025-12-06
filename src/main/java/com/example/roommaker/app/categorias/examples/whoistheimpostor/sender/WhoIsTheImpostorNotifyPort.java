package com.example.roommaker.app.categorias.examples.whoistheimpostor.sender;

import com.example.roommaker.app.categorias.examples.whoistheimpostor.domain.models.WhoIsTheImpostor;
import com.example.roommaker.app.categorias.examples.whoistheimpostor.domain.models.WhoIsTheImpostorResponse;

import java.util.List;

public interface WhoIsTheImpostorNotifyPort {

    void enviarParaUsuario(
            String usernameDono,
            String nomeSala,
            String usuario,
            WhoIsTheImpostorResponse response
    );

    void enviarParaTodos(
            String usernameDono,
            String nomeSala,
            List<String> usuarios,
            WhoIsTheImpostorResponse response
    );

    WhoIsTheImpostorResponse criarResponse(WhoIsTheImpostor jogo, String usuario);
    WhoIsTheImpostorResponse criarResponsePartidaFinalizada(WhoIsTheImpostor jogo);

}
