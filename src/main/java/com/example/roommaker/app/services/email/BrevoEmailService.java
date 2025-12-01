package com.example.roommaker.app.services.email;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import com.example.roommaker.app.domain.models.Email;
import com.example.roommaker.app.domain.ports.email.EmailService;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
@Service
public class BrevoEmailService implements EmailService {

    private final RestTemplate restTemplate;
    private final String apiKey;
    private final String from;

    public BrevoEmailService(@Value("${brevo.api.key}") String apiKey, @Value("${brevo.email.from}") String from) {
        this.apiKey = apiKey;
        this.from = from;
        this.restTemplate = new RestTemplate();
    }

    @Override
    public void sendEmail(Email email) {
        CompletableFuture.runAsync(() -> {
            try {
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                headers.set("api-key", apiKey);

                // 🔧 Correção 1: "to" precisa ser uma LISTA de objetos
                Map<String, Object> payload = new HashMap<>();
                payload.put("sender", Map.of("email", this.from)); // altere aqui para seu remetente
                payload.put("to", List.of(Map.of("email", email.getTo())));
                payload.put("subject", email.getSubject());
                payload.put("htmlContent", "<p>" + email.getBody() + "</p>");

                // 🔧 Correção 2: corpo JSON precisa ser bem formatado
                HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);

                restTemplate.postForEntity(
                        "https://api.brevo.com/v3/smtp/email",
                        request,
                        String.class
                );

                System.out.println("E-mail enviado com sucesso para " + email.getTo());

            } catch (Exception e) {
                throw new RuntimeException("Erro ao enviar e-mail via Brevo API", e);
            }
        });
    }
}
