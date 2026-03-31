package com.gongxifacai.gongxifacai.controller;

import com.gongxifacai.gongxifacai.common.Result;
import com.gongxifacai.gongxifacai.entity.Holding;
import com.gongxifacai.gongxifacai.service.UserService;
import com.gongxifacai.gongxifacai.dto.UserInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {
    @Autowired
    private   UserService userService;
    //    获取用户信息
    @GetMapping("/{id}")
    public Result<UserInfo> getUserInfo(@PathVariable Long id) {
        return Result.success(userService.getUserInfo(id));
    }
    //获取用户持仓情况
    @GetMapping("/{id}/holdings")
    public Result<List<Holding>> getUserHoldings(@PathVariable Long id) {
        return Result.success(userService.getUserHoldings(id));
    }
}
