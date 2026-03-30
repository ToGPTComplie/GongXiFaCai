package com.gongxifacai.gongxifacai.repository;

import com.gongxifacai.gongxifacai.entity.TradeTransaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TradeTransactionRepository extends JpaRepository<TradeTransaction, Long> {

    Page<TradeTransaction> findByUser_Id(Long userId, Pageable pageable);
}
