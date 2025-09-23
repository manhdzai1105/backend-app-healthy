package com.example.chat.listener;

import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class OnlineUserTracker {
    private final Set<String> onlineUserIds = ConcurrentHashMap.newKeySet();

    public void addUser(String userId) {
        onlineUserIds.add(userId);
    }

    public void removeUser(String userId) {
        onlineUserIds.remove(userId);
    }

    public Set<String> getOnlineUsers() {
        return onlineUserIds;
    }
}
