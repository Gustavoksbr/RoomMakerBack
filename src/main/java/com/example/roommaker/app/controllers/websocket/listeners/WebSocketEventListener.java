package com.example.roommaker.app.controllers.websocket.listeners;

import com.example.roommaker.app.controllers.websocket.sala.SalaSenderWebsocket;
import com.example.roommaker.app.controllers.websocket.tracker.OnlineUserTracker;
import com.example.roommaker.app.domain.managers.sala.SalaManager;
import com.example.roommaker.app.domain.managers.usuario.UsuarioManager;
import com.example.roommaker.app.domain.models.Sala;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * Escuta eventos de conexão/desconexão WebSocket
 * Atualiza o rastreador de usuários online
 */
@Component
public class WebSocketEventListener {

    private final OnlineUserTracker onlineUserTracker;
    private final UsuarioManager usuarioManager;
    private final SalaManager salaManager;
    private final SalaSenderWebsocket salaSenderWebsocket;

    @Autowired
    public WebSocketEventListener(
            OnlineUserTracker onlineUserTracker,
            UsuarioManager usuarioManager,
            SalaManager salaManager,
            SalaSenderWebsocket salaSenderWebsocket) {
        this.onlineUserTracker = onlineUserTracker;
        this.usuarioManager = usuarioManager;
        this.salaManager = salaManager;
        this.salaSenderWebsocket = salaSenderWebsocket;
    }

    /**
     * Evento disparado quando um usuário conecta via WebSocket
     */
    @EventListener
    public void handleWebSocketConnectListener(SessionConnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();

        // Extrai o token JWT do header
        String authToken = headerAccessor.getFirstNativeHeader("Authorization");

        if (authToken != null && authToken.startsWith("Bearer ")) {
            try {
                String jwtToken = authToken.substring(7);
                String username = usuarioManager.capturarUsernameDoToken(jwtToken);

                // Adiciona usuário ao tracker
                onlineUserTracker.addUser(username, sessionId);

                // Notifica salas onde o usuário está sobre o status online
                notificarStatusOnlineNasSalas(username, true);

            } catch (Exception e) {
                System.err.println("❌ Erro ao processar conexão WebSocket: " + e.getMessage());
            }
        }
    }

    /**
     * Evento disparado quando um usuário desconecta
     */
    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();

        // Remove usuário do tracker
        String username = onlineUserTracker.removeUser(sessionId);

        // Se o usuário não tem mais nenhuma sessão ativa, notifica que ficou offline
        if (username != null && !onlineUserTracker.isUserOnline(username)) {
            notificarStatusOnlineNasSalas(username, false);
        }
    }

    /**
     * Notifica todas as salas onde o usuário está sobre mudança de status online
     * 
     * @param username Usuário que mudou de status
     * @param online   true se ficou online, false se ficou offline
     */
    private void notificarStatusOnlineNasSalas(String username, boolean online) {
        try {
            // Busca salas onde o usuário é dono
            List<Sala> salasDono = salaManager.listarPorDono(username);

            // Busca salas onde o usuário é participante
            List<Sala> salasParticipante = salaManager.listarPorParticipante(username);

            // Combina todas as salas
            List<Sala> todasSalas = new ArrayList<>();
            todasSalas.addAll(salasDono);
            todasSalas.addAll(salasParticipante);

            // Notifica cada sala sobre a mudança de status
            for (Sala sala : todasSalas) {
                List<String> ouvintes = new ArrayList<>(sala.getUsernameParticipantes());
                ouvintes.add(sala.getUsernameDono());

                // Remove o próprio usuário dos ouvintes (ele já sabe que está online/offline)
                ouvintes.remove(username);

                if (!ouvintes.isEmpty()) {
                    salaSenderWebsocket.enviarMensagemParaSala(
                            sala.getUsernameDono(),
                            sala.getNome(),
                            "status-online",
                            ouvintes,
                            new StatusOnlineNotification(username, online));
                }
            }
        } catch (Exception e) {
            System.err.println("❌ Erro ao notificar status online: " + e.getMessage());
        }
    }

    /**
     * DTO para notificação de status online
     */
    public static class StatusOnlineNotification {
        private String username;
        private boolean online;

        public StatusOnlineNotification(String username, boolean online) {
            this.username = username;
            this.online = online;
        }

        public String getUsername() {
            return username;
        }

        public boolean isOnline() {
            return online;
        }
    }
}
