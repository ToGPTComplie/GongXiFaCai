package com.gongxifacai.gongxifacai.dto;

import com.gongxifacai.gongxifacai.entity.User;
import java.math.BigDecimal;

public record UserInfo(Long id, String name, BigDecimal availableCash) {
    public static UserInfo fromEntity(User user) {
        return new UserInfo(user.getId(), user.getName(), user.getAvailableCash());
    }
}
