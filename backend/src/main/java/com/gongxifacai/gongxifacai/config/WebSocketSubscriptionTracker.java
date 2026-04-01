package com.gongxifacai.gongxifacai.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;
import org.springframework.web.socket.messaging.SessionUnsubscribeEvent;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class WebSocketSubscriptionTracker {

    private final Map<String, Map<String, String>> sessionSubscriptions = new ConcurrentHashMap<>();

    @EventListener
    public void handleSubscribe(SessionSubscribeEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = accessor.getSessionId();
        String subscriptionId = accessor.getSubscriptionId();
        String destination = accessor.getDestination();

        if (sessionId == null || subscriptionId == null || destination == null) {
            return;
        }

        sessionSubscriptions
                .computeIfAbsent(sessionId, ignored -> new ConcurrentHashMap<>())
                .put(subscriptionId, destination);

        log.info("WebSocket subscribe: sessionId={}, subscriptionId={}, destination={}",
                sessionId, subscriptionId, destination);
    }

    @EventListener
    public void handleUnsubscribe(SessionUnsubscribeEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = accessor.getSessionId();
        String subscriptionId = accessor.getSubscriptionId();

        if (sessionId == null || subscriptionId == null) {
            return;
        }

        removeSubscription(sessionId, subscriptionId);
    }

    @EventListener
    public void handleDisconnect(SessionDisconnectEvent event) {
        String sessionId = event.getSessionId();
        if (sessionId == null) {
            return;
        }

        Map<String, String> removed = sessionSubscriptions.remove(sessionId);
        if (removed != null && !removed.isEmpty()) {
            log.info("WebSocket disconnect: sessionId={}, removedSubscriptions={}", sessionId, removed.size());
        }
    }

    public boolean hasSubscribers(String destination) {
        return sessionSubscriptions.values().stream()
                .flatMap(subscriptions -> subscriptions.values().stream())
                .anyMatch(destination::equals);
    }

    private void removeSubscription(String sessionId, String subscriptionId) {
        Map<String, String> subscriptions = sessionSubscriptions.get(sessionId);
        if (subscriptions == null) {
            return;
        }

        String removedDestination = subscriptions.remove(subscriptionId);
        if (subscriptions.isEmpty()) {
            sessionSubscriptions.remove(sessionId);
        }

        if (removedDestination != null) {
            log.info("WebSocket unsubscribe: sessionId={}, subscriptionId={}, destination={}",
                    sessionId, subscriptionId, removedDestination);
        }
    }
}
