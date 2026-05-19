package com.example.roommaker.app.controllers.websocket.sala;

import com.example.roommaker.app.controllers.websocket.tracker.OnlineUserTracker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class SalaSenderWebsocket {
    private final SimpMessagingTemplate messagingTemplate;
    private final OnlineUserTracker onlineUserTracker;

    @Autowired
    public SalaSenderWebsocket(SimpMessagingTemplate messagingTemplate, OnlineUserTracker onlineUserTracker) {
        this.messagingTemplate = messagingTemplate;
        this.onlineUserTracker = onlineUserTracker;
    }

    public void enviarMensagemParaSala(String usernameDono, String salaNome, String tipo, List<String> ouvintes,
            Object mensagem) {
        for (String ouvinte : ouvintes) {
            String destino = "/topic/sala/" + usernameDono + "/" + salaNome + "/" + ouvinte + "/" + tipo;
            if (mensagem != null) {
                this.messagingTemplate.convertAndSend(destino, mensagem);
            }
        }
    }

    /**
     * Notifica APENAS usuários online sobre atualizações de salas
     * Usado nas páginas de listagem (/salas e /suas-salas)
     * 
     * @param tipo     Tipo de evento: "CRIADA", "DELETADA", "ATUALIZADA"
     * @param mensagem Objeto a ser enviado (geralmente SalaResponse)
     */
    public void notificarAtualizacaoDeSalasParaUsuariosOnline(String tipo, Object mensagem) {
        Set<String> usuariosOnline = onlineUserTracker.getOnlineUsers();

        System.out.println("📢 Notificando " + usuariosOnline.size() + " usuários online sobre evento: " + tipo);

        for (String username : usuariosOnline) {
            String destino = "/topic/user/" + username + "/salas/updates";
            this.messagingTemplate.convertAndSend(destino, new SalaUpdateNotification(tipo, mensagem));
        }
    }

    /**
     * Retorna lista de usuários online (útil para adicionar ao response)
     */
    public Set<String> getUsuariosOnline() {
        return onlineUserTracker.getOnlineUsers();
    }

    /**
     * Verifica se um usuário específico está online
     */
    public boolean isUsuarioOnline(String username) {
        return onlineUserTracker.isUserOnline(username);
    }

    /**
     * DTO para notificação de atualização de sala
     */
    public static class SalaUpdateNotification {
        private String tipo; // CRIADA, DELETADA, ATUALIZADA
        private Object sala;

        public SalaUpdateNotification(String tipo, Object sala) {
            this.tipo = tipo;
            this.sala = sala;
        }

        public String getTipo() {
            return tipo;
        }

        public Object getSala() {
            return sala;
        }
    }
}
