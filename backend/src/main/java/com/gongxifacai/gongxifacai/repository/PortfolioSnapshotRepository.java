package com.gongxifacai.gongxifacai.repository;

import com.gongxifacai.gongxifacai.entity.PortfolioSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface PortfolioSnapshotRepository extends JpaRepository<PortfolioSnapshot, Long> {

    // upsert 时先查当天是否已有快照
    Optional<PortfolioSnapshot> findByUser_IdAndSnapshotDate(Long userId, LocalDate snapshotDate);

    // 折线图查询：按日期范围升序返回
    List<PortfolioSnapshot> findByUser_IdAndSnapshotDateBetweenOrderBySnapshotDateAsc(
            Long userId, LocalDate startDate, LocalDate endDate);
}

