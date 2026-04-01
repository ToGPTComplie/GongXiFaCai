package com.gongxifacai.gongxifacai.service.impl;

import com.gongxifacai.gongxifacai.config.WebSocketSubscriptionTracker;
import com.gongxifacai.gongxifacai.dto.HoldingDTO;
import com.gongxifacai.gongxifacai.service.HoldingPushService;
import com.gongxifacai.gongxifacai.service.HoldingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class HoldingPushServiceImpl implements HoldingPushService {

    private final HoldingService holdingService;
    private final SimpMessagingTemplate simpMessagingTemplate;
    private final WebSocketSubscriptionTracker subscriptionTracker;
    @Value("${app.push.user-id:1}")
    private Long pushUserId;

    @EventListener
    public void handleSubscription(SessionSubscribeEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String destination = accessor.getDestination();
        String targetDestination = buildDestination();

        if (!targetDestination.equals(destination)) {
            return;
        }

        log.info("Initial holdings push requested on subscribe, destination={}", destination);
        pushHoldingsSnapshot(targetDestination);
    }

    @Override
    @Scheduled(fixedRate = 60_000)
    public void pushLatestHoldings() {
        String destination = buildDestination();
        boolean hasSubscription = subscriptionTracker.hasSubscribers(destination);

        log.info("pushLatestHoldings triggered, destination={}, hasSubscription={}", destination, hasSubscription);

        if (!hasSubscription) {
            return;
        }

        pushHoldingsSnapshot(destination);
    }

    private String buildDestination() {
        return "/topic/users/" + pushUserId + "/holdings";
    }

    private void pushHoldingsSnapshot(String destination) {
        try {
            List<HoldingDTO> holdings = holdingService.getUserHoldingsWithprice(pushUserId);
            log.info("sending holdings update, userId={}, count={}", pushUserId, holdings.size());
            simpMessagingTemplate.convertAndSend(destination, holdings);
        } catch (Exception ex) {
            log.warn("Push holdings failed for userId={}", pushUserId, ex);
        }
    }

}
