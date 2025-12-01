package com.example.roommaker.app.controllers.http.ping;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/ping")
public class PingController {
    @GetMapping
    public String ping() {
        return "pong";
    }

    private final RestTemplate restTemplate = new RestTemplate();

    // Troque pela URL pública da sua aplicação no Render
    // http://localhost:8080/testando

    private static final String SELF_URL = "https://roommaker-2-7.onrender.com/ping";

    // A cada 10 minutos (10 * 60 * 1000 ms)
    @Scheduled(fixedRate = 10 * 60 * 1000)
    public void scheduledHello() {
        var now = LocalDateTime.now();
        var formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String formattedNow = now.format(formatter);
        restTemplate.getForObject(SELF_URL, String.class);
        System.out.println("Auto-Ping OK [" + formattedNow + "]: ");
    }
}
