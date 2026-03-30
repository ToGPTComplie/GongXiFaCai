package com.gongxifacai.gongxifacai.repository;

import com.gongxifacai.gongxifacai.entity.FundTransaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FundTransactionRepository extends JpaRepository<FundTransaction, Long> {

    Page<FundTransaction> findByUser_Id(Long userId, Pageable pageable);
}
