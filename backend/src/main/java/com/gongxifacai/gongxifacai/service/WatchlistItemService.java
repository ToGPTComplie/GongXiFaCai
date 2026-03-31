package com.gongxifacai.gongxifacai.service;

import com.gongxifacai.gongxifacai.dto.WatchlistItemRequestDTO;
import com.gongxifacai.gongxifacai.dto.WatchlistItemResponseDTO;

import java.util.List;

public interface WatchlistItemService {

    WatchlistItemResponseDTO addToWatchlist(Long userId, WatchlistItemRequestDTO dto);

    List<WatchlistItemResponseDTO> getWatchlist(Long userId);

    WatchlistItemResponseDTO updateWatchlistItem(Long userId, Long id, WatchlistItemRequestDTO dto);

    void deleteWatchlistItem(Long userId, Long id);

    boolean isInWatchlist(Long userId, String ticker);
}

