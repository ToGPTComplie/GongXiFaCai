package com.gongxifacai.gongxifacai.repository;

import com.gongxifacai.gongxifacai.entity.TradeTransaction;

import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.stereotype.Repository;

@Repository
public interface TradeTransactionRepository extends JpaRepository<TradeTransaction, Long> {
}
