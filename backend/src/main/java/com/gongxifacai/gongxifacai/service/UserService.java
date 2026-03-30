package com.gongxifacai.gongxifacai.service;

import com.gongxifacai.gongxifacai.entity.User;
import com.gongxifacai.gongxifacai.service.impl.UserServiceImpl.UserInfo;

import java.math.BigDecimal;

public interface UserService {

    User getUser(Long userId);

    UserInfo getUserInfo(Long userId);

    User createUser(String name, BigDecimal initialCash);
}
