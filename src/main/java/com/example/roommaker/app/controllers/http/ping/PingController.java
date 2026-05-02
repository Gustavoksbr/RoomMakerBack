package com.example.roommaker.app.controllers.http.ping;

import org.springframework.beans.factory.annotation.Value;
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

    @Value("${self.ping.url}")
    private String selfUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    @GetMapping
    public String ping() {
        return "pong";
    }

    // A cada 10 minutos (10 * 60 * 1000 ms)
    @Scheduled(fixedRate = 10 * 60 * 1000)
    public void scheduledHello() {
        var now = LocalDateTime.now();
        var formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String formattedNow = now.format(formatter);
        restTemplate.getForObject(selfUrl, String.class);
        System.out.println("Auto-Ping OK [" + formattedNow + "]: ");
    }
}
