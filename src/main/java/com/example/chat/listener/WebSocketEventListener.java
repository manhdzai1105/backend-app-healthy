package com.example.chat.listener;

import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.*;

import java.security.Principal;

@Component
@RequiredArgsConstructor
public class WebSocketEventListener {

    private final OnlineUserTracker onlineUserTracker;
    private final SimpMessagingTemplate messagingTemplate;

    @EventListener
    public void handleSessionConnected(SessionConnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        Principal user = accessor.getUser();

        if (user != null) {
            String userId = user.getName();
            System.out.println("🔌 CONNECTED: " + userId);

            onlineUserTracker.addUser(userId);
            messagingTemplate.convertAndSend("/topic/online-users", onlineUserTracker.getOnlineUsers());
        } else {
            System.out.println("⚠️ CONNECTED: user is null");
        }
    }

    @EventListener
    public void handleSessionDisconnect(SessionDisconnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        Principal user = accessor.getUser();

        if (user != null) {
            String userId = user.getName();
            System.out.println("❌ DISCONNECTED: " + userId);

            onlineUserTracker.removeUser(userId);
            messagingTemplate.convertAndSend("/topic/online-users", onlineUserTracker.getOnlineUsers());
        } else {
            System.out.println("⚠️ DISCONNECTED: user is null");
        }
    }
}
