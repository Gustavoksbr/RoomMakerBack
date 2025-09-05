package com.example.roommaker.app.domain.ports.make;

import com.example.roommaker.app.domain.models.Usuario;

public interface MakeWebhook {
    void sendUser(Usuario usuario);
}
