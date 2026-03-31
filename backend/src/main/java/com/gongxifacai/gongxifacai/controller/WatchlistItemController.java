package com.gongxifacai.gongxifacai.controller;

import com.gongxifacai.gongxifacai.common.Result;
import com.gongxifacai.gongxifacai.dto.WatchlistItemRequestDTO;
import com.gongxifacai.gongxifacai.dto.WatchlistItemResponseDTO;
import com.gongxifacai.gongxifacai.service.WatchlistItemService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class WatchlistItemController {

    private final WatchlistItemService watchlistItemService;

    // 添加到自选
    @PostMapping("/{userId}/watchlist")
    public Result<WatchlistItemResponseDTO> addToWatchlist(
            @PathVariable Long userId,
            @Valid @RequestBody WatchlistItemRequestDTO dto) {
        return Result.success(watchlistItemService.addToWatchlist(userId, dto));
    }

    // 查询自选列表
    @GetMapping("/{userId}/watchlist")
    public Result<List<WatchlistItemResponseDTO>> getWatchlist(@PathVariable Long userId) {
        return Result.success(watchlistItemService.getWatchlist(userId));
    }

    // 更新备注/目标价（ticker 和 assetType 不可修改）
    @PutMapping("/{userId}/watchlist/{id}")
    public Result<WatchlistItemResponseDTO> updateWatchlistItem(
            @PathVariable Long userId,
            @PathVariable Long id,
            @RequestBody WatchlistItemRequestDTO dto) {
        return Result.success(watchlistItemService.updateWatchlistItem(userId, id, dto));
    }

    // 删除自选标的
    @DeleteMapping("/{userId}/watchlist/{id}")
    public Result<Void> deleteWatchlistItem(
            @PathVariable Long userId,
            @PathVariable Long id) {
        watchlistItemService.deleteWatchlistItem(userId, id);
        return Result.success();
    }

    // 检查是否已在自选
    @GetMapping("/{userId}/watchlist/check")
    public Result<Boolean> checkInWatchlist(
            @PathVariable Long userId,
            @RequestParam String ticker) {
        return Result.success(watchlistItemService.isInWatchlist(userId, ticker));
    }
}

