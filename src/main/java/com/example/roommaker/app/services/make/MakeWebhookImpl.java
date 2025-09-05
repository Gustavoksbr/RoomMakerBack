package com.example.roommaker.app.services.make;

import com.example.roommaker.app.domain.models.Usuario;
import com.example.roommaker.app.domain.ports.make.MakeWebhook;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;


@Service
public class MakeWebhookImpl implements MakeWebhook {
    private final RestTemplate restTemplate = new RestTemplate();
    @Override
    public void sendUser(Usuario usuario) {

        this.restTemplate.postForLocation("https://hook.us2.make.com/trv2ev8jw9alglunpnw2jv1nwtvzviyj", usuario);
    }
}
