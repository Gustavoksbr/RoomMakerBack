package com.example.roommaker.app.services.email;

import com.example.roommaker.app.domain.models.Email;
import com.example.roommaker.app.domain.ports.email.EmailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Envio de e-mail via Resend (https://resend.com), no lugar do Brevo — cuja
 * chave de API a conta desativou por inatividade (ver histórico do projeto).
 *
 * O free tier do Resend manda de produção (100/dia, 3000/mês) sem precisar
 * verificar domínio, DESDE QUE o remetente seja {@code onboarding@resend.dev}
 * (o domínio deles, já autenticado). Um remetente em domínio próprio só é
 * aceito depois de publicar os registros DNS de verificação — e {@code
 * resend.email.from} vindo de um domínio que ninguém aqui é dono (como
 * gmail.com) é rejeitado pela própria API do Resend, sem nem chegar a tentar
 * entregar o e-mail.
 */
@Service
@Slf4j
public class ResendEmailService implements EmailService {

    private static final String API_URL = "https://api.resend.com/emails";

    private final RestTemplate restTemplate;
    private final String apiKey;
    private final String from;

    public ResendEmailService(@Value("${resend.api.key}") String apiKey, @Value("${resend.email.from}") String from) {
        this.apiKey = apiKey;
        this.from = from;
        this.restTemplate = new RestTemplate();
    }

    @Override
    public void sendEmail(Email email) {
        // Fire-and-forget: a requisição HTTP que pediu o e-mail (ex.: "esqueci
        // minha senha") não fica esperando o Resend responder. Cada resultado vai
        // pro log — sucesso e falha —, porque um envio que falha em silêncio
        // parece bug em outro lugar completamente diferente.
        CompletableFuture.runAsync(() -> {
            try {
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                headers.setBearerAuth(apiKey);

                Map<String, Object> payload = new HashMap<>();
                payload.put("from", from);
                payload.put("to", List.of(email.getTo()));
                payload.put("subject", email.getSubject());
                payload.put("html", "<p>" + email.getBody() + "</p>");

                HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);

                ResponseEntity<String> resposta = restTemplate.postForEntity(API_URL, request, String.class);

                HttpStatusCode status = resposta.getStatusCode();
                log.info("E-mail enviado via Resend para {} (remetente: {}) — status {}: {}",
                        email.getTo(), from, status, resposta.getBody());
            } catch (RestClientResponseException e) {
                // A API do Resend respondeu, mas recusou — o corpo costuma dizer o
                // motivo exato (chave inválida, remetente de domínio não
                // verificado, etc).
                log.error("Resend recusou o e-mail para {} (remetente: {}) — status {}: {}",
                        email.getTo(), from, e.getStatusCode(), e.getResponseBodyAsString());
            } catch (Exception e) {
                log.error("Falha ao chamar a API do Resend para enviar e-mail a {} (remetente: {})",
                        email.getTo(), from, e);
            }
        });
    }
}
