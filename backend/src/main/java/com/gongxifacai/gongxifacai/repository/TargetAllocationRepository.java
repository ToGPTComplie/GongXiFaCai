package com.gongxifacai.gongxifacai.repository;

import com.gongxifacai.gongxifacai.entity.TargetAllocation;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TargetAllocationRepository extends JpaRepository<TargetAllocation, Long> {
}
