package com.example.roommaker.app.controllers.websocket.tracker;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Rastreia usuários conectados via WebSocket
 * Thread-safe usando ConcurrentHashMap
 */
@Component
public class OnlineUserTracker {

    // sessionId -> username
    private final Map<String, String> sessionToUsername = new ConcurrentHashMap<>();

    // username -> Set<sessionId> (um usuário pode ter múltiplas sessões/abas
    // abertas)
    private final Map<String, Set<String>> usernameToSessions = new ConcurrentHashMap<>();

    /**
     * Adiciona um usuário online
     * 
     * @param username  Nome do usuário
     * @param sessionId ID da sessão WebSocket
     */
    public void addUser(String username, String sessionId) {
        sessionToUsername.put(sessionId, username);
        usernameToSessions.computeIfAbsent(username, k -> ConcurrentHashMap.newKeySet()).add(sessionId);
        System.out.println("✅ Usuário conectado: " + username + " (sessão: " + sessionId + ")");
        System.out.println("📊 Total online: " + getOnlineUserCount());
    }

    /**
     * Remove um usuário quando desconecta
     * 
     * @param sessionId ID da sessão WebSocket
     * @return Username que foi removido, ou null se não encontrado
     */
    public String removeUser(String sessionId) {
        String username = sessionToUsername.remove(sessionId);
        if (username != null) {
            Set<String> sessions = usernameToSessions.get(username);
            if (sessions != null) {
                sessions.remove(sessionId);
                // Se não tem mais sessões, remove o usuário completamente
                if (sessions.isEmpty()) {
                    usernameToSessions.remove(username);
                }
            }
            System.out.println("❌ Usuário desconectado: " + username + " (sessão: " + sessionId + ")");
            System.out.println("📊 Total online: " + getOnlineUserCount());
        }
        return username;
    }

    /**
     * Verifica se um usuário está online
     * 
     * @param username Nome do usuário
     * @return true se o usuário tem pelo menos uma sessão ativa
     */
    public boolean isUserOnline(String username) {
        Set<String> sessions = usernameToSessions.get(username);
        return sessions != null && !sessions.isEmpty();
    }

    /**
     * Retorna todos os usuários online
     * 
     * @return Set com usernames de todos os usuários online
     */
    public Set<String> getOnlineUsers() {
        return usernameToSessions.keySet();
    }

    /**
     * Retorna quantidade de usuários online
     * 
     * @return Número de usuários únicos online
     */
    public int getOnlineUserCount() {
        return usernameToSessions.size();
    }

    /**
     * Retorna quantidade de sessões ativas de um usuário
     * 
     * @param username Nome do usuário
     * @return Número de sessões (abas/dispositivos) do usuário
     */
    public int getUserSessionCount(String username) {
        Set<String> sessions = usernameToSessions.get(username);
        return sessions != null ? sessions.size() : 0;
    }

    /**
     * Limpa todos os usuários (útil para testes)
     */
    public void clear() {
        sessionToUsername.clear();
        usernameToSessions.clear();
    }
}
