package com.gongxifacai.gongxifacai.repository;

import com.gongxifacai.gongxifacai.entity.User;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
}
