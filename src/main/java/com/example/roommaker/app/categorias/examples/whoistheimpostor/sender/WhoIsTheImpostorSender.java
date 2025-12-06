package com.example.roommaker.app.categorias.examples.whoistheimpostor.sender;

import com.example.roommaker.app.categorias.examples.whoistheimpostor.domain.models.WhoIsTheImpostorResponse;
import com.example.roommaker.app.categorias.examples.whoistheimpostor.domain.models.WhoIsTheImpostor;
import com.example.roommaker.app.controllers.websocket.sala.SalaSenderWebsocket;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class WhoIsTheImpostorSender  implements WhoIsTheImpostorNotifyPort {

    private final SalaSenderWebsocket salaSenderWebsocket;

    @Autowired
    public WhoIsTheImpostorSender(@Lazy SalaSenderWebsocket salaSenderWebsocket) {
        this.salaSenderWebsocket = salaSenderWebsocket;
    }
    @Override
    public void enviarParaUsuario(
            String usernameDono,
            String nomeSala,
            String usuario,
            WhoIsTheImpostorResponse response
    ) {
        salaSenderWebsocket.enviarMensagemParaSala(
                usernameDono,
                nomeSala,
                "whoistheimpostor",
                List.of(usuario),
                response
        );
    }
    @Override
    public void enviarParaTodos(
            String usernameDono,
            String nomeSala,
            List<String> usuarios,
            WhoIsTheImpostorResponse response
    ) {
        salaSenderWebsocket.enviarMensagemParaSala(
                usernameDono,
                nomeSala,
                "whoistheimpostor",
                usuarios,
                response
        );
    }

    // ===========================
    // CRIAÇÃO DAS RESPONSES
    // ===========================
@Override
    public WhoIsTheImpostorResponse criarResponse(WhoIsTheImpostor jogo, String usuario) {

        WhoIsTheImpostorResponse r = new WhoIsTheImpostorResponse();

        if (!jogo.getJogando()) {
            r.setPartidaSendoJogada(false);
            r.setImpostorDaPartidaPassada(jogo.getImpostor());
            r.setCartaDaPartidaPassada(jogo.getCarta());
            return r;
        }

        r.setPartidaSendoJogada(true);

        if (!jogo.getJogadores().contains(usuario)) {
            r.setEstaNaPartida(false);
            return r;
        }

        r.setEstaNaPartida(true);

        boolean impostor = jogo.getImpostor().equals(usuario);
        r.setIsImpostor(impostor);
        r.setCarta(impostor ? null : jogo.getCarta());

        return r;
    }
    @Override
    public WhoIsTheImpostorResponse criarResponsePartidaFinalizada(WhoIsTheImpostor jogo) {
        WhoIsTheImpostorResponse r = new WhoIsTheImpostorResponse();
        r.setPartidaSendoJogada(false);
        r.setImpostorDaPartidaPassada(jogo.getImpostor());
        r.setCartaDaPartidaPassada(jogo.getCarta());
        return r;
    }
}
