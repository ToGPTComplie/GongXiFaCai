package com.gongxifacai.gongxifacai.scheduler;

import com.gongxifacai.gongxifacai.entity.User;
import com.gongxifacai.gongxifacai.service.PortfolioSnapshotService;
import com.gongxifacai.gongxifacai.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

/**
 * 收盘后定时任务：为所有用户生成当日 Portfolio 盈亏快照
 * 触发时间：美东时间周一至周五 16:30（美股收盘后）
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MarketCloseScheduler {

    private final PortfolioSnapshotService portfolioSnapshotService;
    private final UserService userService;

    @Scheduled(cron = "0 30 16 * * MON-FRI", zone = "America/New_York")
    public void generateDailySnapshots() {
        LocalDate today = LocalDate.now(ZoneId.of("America/New_York"));
        log.info("收盘快照任务开始，日期：{}", today);

        List<User> allUsers = userService.getAllUsers();

        for (User user : allUsers) {
            try {
                portfolioSnapshotService.createOrUpdateSnapshot(user.getId(), today);
                log.info("用户 {} 快照生成成功", user.getId());
            } catch (Exception e) {
                // 单个用户失败不影响其他用户
                log.error("用户 {} 快照生成失败：{}", user.getId(), e.getMessage());
            }
        }

        log.info("收盘快照任务完成，共处理 {} 个用户", allUsers.size());
    }
}
