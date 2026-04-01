package com.gongxifacai.gongxifacai.repository;

import com.gongxifacai.gongxifacai.entity.TargetAllocation;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TargetAllocationRepository extends JpaRepository<TargetAllocation, Long> {

    List<TargetAllocation> findByUser_Id(Long userId);

    void deleteAllByUser_Id(Long userId);
}
