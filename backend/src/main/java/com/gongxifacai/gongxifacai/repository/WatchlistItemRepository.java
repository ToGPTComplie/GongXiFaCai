package com.gongxifacai.gongxifacai.repository;

import com.gongxifacai.gongxifacai.entity.WatchlistItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WatchlistItemRepository extends JpaRepository<WatchlistItem, Long> {

    List<WatchlistItem> findByUserId(Long userId);

    boolean existsByUserIdAndTicker(Long userId, String ticker);

    Optional<WatchlistItem> findByIdAndUserId(Long id, Long userId);
}

