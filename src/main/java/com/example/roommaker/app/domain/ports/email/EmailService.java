package com.example.roommaker.app.domain.ports.email;

import com.example.roommaker.app.domain.models.Email;

public interface EmailService {
    void sendEmail(Email email);
}
