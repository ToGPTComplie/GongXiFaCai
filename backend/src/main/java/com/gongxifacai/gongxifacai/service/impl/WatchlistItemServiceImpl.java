package com.gongxifacai.gongxifacai.service.impl;

import com.gongxifacai.gongxifacai.dto.WatchlistItemRequestDTO;
import com.gongxifacai.gongxifacai.dto.WatchlistItemResponseDTO;
import com.gongxifacai.gongxifacai.entity.User;
import com.gongxifacai.gongxifacai.entity.WatchlistItem;
import com.gongxifacai.gongxifacai.exception.BusinessException;
import com.gongxifacai.gongxifacai.repository.WatchlistItemRepository;
import com.gongxifacai.gongxifacai.service.UserService;
import com.gongxifacai.gongxifacai.service.WatchlistItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.gongxifacai.gongxifacai.common.CommonErrorCode.WATCHLIST_ITEM_ALREADY_EXISTS;
import static com.gongxifacai.gongxifacai.common.CommonErrorCode.WATCHLIST_ITEM_NOT_FOUND;

@Service
@RequiredArgsConstructor
public class WatchlistItemServiceImpl implements WatchlistItemService {

    private final WatchlistItemRepository watchlistItemRepository;
    private final UserService userService;

    @Override
    public WatchlistItemResponseDTO addToWatchlist(Long userId, WatchlistItemRequestDTO dto) {
        User user = userService.getUser(userId);

        // 同一用户不能重复添加相同 ticker
        if (watchlistItemRepository.existsByUserIdAndTicker(userId, dto.getTicker())) {
            throw new BusinessException(WATCHLIST_ITEM_ALREADY_EXISTS);
        }

        WatchlistItem item = new WatchlistItem();
        item.setUser(user);
        item.setTicker(dto.getTicker());
        item.setAssetType(dto.getAssetType());
        item.setNotes(dto.getNotes());
        item.setTargetBuyPrice(dto.getTargetBuyPrice());
        item.setAlertPriceHigh(dto.getAlertPriceHigh());
        item.setAlertPriceLow(dto.getAlertPriceLow());

        return WatchlistItemResponseDTO.from(watchlistItemRepository.save(item));
    }

    @Override
    public List<WatchlistItemResponseDTO> getWatchlist(Long userId) {
        userService.getUser(userId); // 验证用户存在
        return watchlistItemRepository.findByUserId(userId)
                .stream()
                .map(WatchlistItemResponseDTO::from)
                .toList();
    }

    @Override
    public WatchlistItemResponseDTO updateWatchlistItem(Long userId, Long id, WatchlistItemRequestDTO dto) {
        userService.getUser(userId); // 验证用户存在

        // 校验该条目存在且属于当前用户（防越权）
        WatchlistItem item = watchlistItemRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new BusinessException(WATCHLIST_ITEM_NOT_FOUND));

        // 方案 B：ticker 和 assetType 不可修改，只更新以下字段
        item.setNotes(dto.getNotes());
        item.setTargetBuyPrice(dto.getTargetBuyPrice());
        item.setAlertPriceHigh(dto.getAlertPriceHigh());
        item.setAlertPriceLow(dto.getAlertPriceLow());

        return WatchlistItemResponseDTO.from(watchlistItemRepository.save(item));
    }

    @Override
    public void deleteWatchlistItem(Long userId, Long id) {
        userService.getUser(userId); // 验证用户存在

        // 校验该条目存在且属于当前用户（防越权）
        WatchlistItem item = watchlistItemRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new BusinessException(WATCHLIST_ITEM_NOT_FOUND));

        watchlistItemRepository.delete(item);
    }

    @Override
    public boolean isInWatchlist(Long userId, String ticker) {
        userService.getUser(userId); // 验证用户存在
        return watchlistItemRepository.existsByUserIdAndTicker(userId, ticker);
    }
}

