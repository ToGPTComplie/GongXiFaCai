package com.gongxifacai.gongxifacai.service.impl;

import com.gongxifacai.gongxifacai.dto.HoldingDTO;
import com.gongxifacai.gongxifacai.service.HoldingPushService;
import com.gongxifacai.gongxifacai.service.HoldingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.user.SimpSubscription;
import org.springframework.messaging.simp.user.SimpUser;
import org.springframework.messaging.simp.user.SimpUserRegistry;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class HoldingPushServiceImpl implements HoldingPushService {

    private final HoldingService holdingService;
    private final SimpMessagingTemplate simpMessagingTemplate;
    private final SimpUserRegistry simpUserRegistry;
    @Value("${app.push.user-id:1}")
    private Long pushUserId;

    @Override
    @Scheduled(fixedRate = 60_000)
    public void pushLatestHoldings() {
        String destination = "/topic/users/" + pushUserId + "/holdings";
        if (!hasActiveSubscription(destination)) {
            return;
        }
        try {
            List<HoldingDTO> holdings = holdingService.getUserHoldingsWithprice(pushUserId);
            simpMessagingTemplate.convertAndSend(destination, holdings);
        } catch (Exception ex) {
            log.warn("Push holdings failed for userId={}", pushUserId, ex);
        }
    }

    private boolean hasActiveSubscription(String destination) {
        for (SimpUser user : simpUserRegistry.getUsers()) {
            for (SimpSubscription subscription : user.getSessions()
                    .stream()
                    .flatMap(session -> session.getSubscriptions().stream())
                    .toList()) {
                if (destination.equals(subscription.getDestination())) {
                    return true;
                }
            }
        }
        return false;
    }
}
