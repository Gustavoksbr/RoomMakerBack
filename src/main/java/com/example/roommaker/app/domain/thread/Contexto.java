package com.example.roommaker.app.domain.thread;

import com.example.roommaker.app.domain.models.Sala;

public class Contexto {
    private static final ThreadLocal<String> usernameThreadLocal = new ThreadLocal<>();
    private static final ThreadLocal<Sala> salaThreadLocal = new ThreadLocal<>();

    public static void setUsername(String username) {
        usernameThreadLocal.set(username);
    }
    public static String getUsername() {
        return usernameThreadLocal.get();
    }

    public static void setSala(Sala sala) {
        salaThreadLocal.set(sala);
    }
    public static Sala getSala() {
        return salaThreadLocal.get();
    }

    public static void clear() {
        usernameThreadLocal.remove();
        salaThreadLocal.remove();
    }
}
