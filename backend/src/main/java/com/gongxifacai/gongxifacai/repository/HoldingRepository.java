package com.gongxifacai.gongxifacai.repository;

import com.gongxifacai.gongxifacai.entity.Holding;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HoldingRepository extends JpaRepository<Holding, Long> {
}
