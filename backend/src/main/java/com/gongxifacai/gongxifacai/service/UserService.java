package com.gongxifacai.gongxifacai.service;

import com.gongxifacai.gongxifacai.entity.Holding;
import com.gongxifacai.gongxifacai.entity.User;
import com.gongxifacai.gongxifacai.dto.UserInfo;

import java.math.BigDecimal;
import java.util.List;

public interface UserService {

    User getUser(Long userId);

    UserInfo getUserInfo(Long userId);

    //  用户持仓情况
    List<Holding> getUserHoldings(Long userId);

    User createUser(String name, BigDecimal initialCash);

    Boolean existsById(Long userId);

    void save(User user);

    User getReferenceById(Long userId);

    List<User> getAllUsers();
}
