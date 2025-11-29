package com.smartrental.backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Endpoint để Client (React Native) kết nối vào
        // URL: ws://localhost:8080/ws
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .withSockJS(); // Hỗ trợ fallback
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // Prefix cho tin nhắn từ Server --> Client
        registry.enableSimpleBroker("/topic", "/queue");

        // Prefix cho tin nhắn từ Client --> Server
        registry.setApplicationDestinationPrefixes("/app");

        // Prefix riêng cho tin nhắn cá nhân (1-1)
        registry.setUserDestinationPrefix("/user");
    }
}