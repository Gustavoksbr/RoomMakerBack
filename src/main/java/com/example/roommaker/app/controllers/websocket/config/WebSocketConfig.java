package com.example.roommaker.app.controllers.websocket.config;

import com.example.roommaker.app.controllers.websocket.filters.JwtWebsocketInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;


@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    @Value("${cors.allowed-origins}")
    private String[] allowedOrigins;

    @Autowired
    private JwtWebsocketInterceptor jwtWebsocketInterceptor;
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/socket")
                .setAllowedOrigins(allowedOrigins);
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.setApplicationDestinationPrefixes("/app"); // define o prefixo de entrada para os endpoints
        config.enableSimpleBroker("/topic"); // define o prefixo de saída para os endpoints
    }

@Override
public void configureClientInboundChannel(ChannelRegistration registration) {
    registration.interceptors(jwtWebsocketInterceptor);

}
}
