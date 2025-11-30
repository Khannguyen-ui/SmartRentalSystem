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
        // Endpoint để Client kết nối vào: ws://localhost:8080/ws
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*") // Cho phép React Native/Web kết nối
                .withSockJS(); // Fallback nếu không hỗ trợ WS thuần
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // Prefix cho các đường dẫn mà Client muốn gửi tin lên
        registry.setApplicationDestinationPrefixes("/app");

        // Prefix cho các đường dẫn mà Client muốn lắng nghe (Subscribe)
        registry.enableSimpleBroker("/topic", "/queue", "/user");

        // Prefix riêng tư cho từng user
        registry.setUserDestinationPrefix("/user");
    }
}