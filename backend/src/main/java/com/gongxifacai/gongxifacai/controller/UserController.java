package com.gongxifacai.gongxifacai.controller;
import com.gongxifacai.gongxifacai.common.CommonErrorCode;
import com.gongxifacai.gongxifacai.common.Result;
import com.gongxifacai.gongxifacai.dto.KLineCandleDTO;
import com.gongxifacai.gongxifacai.dto.TopBottomHoldingsResponseDTO;
import com.gongxifacai.gongxifacai.entity.Holding;
import com.gongxifacai.gongxifacai.exception.BusinessException;
import com.gongxifacai.gongxifacai.service.HoldingService;
import com.gongxifacai.gongxifacai.service.UserService;
import com.gongxifacai.gongxifacai.dto.UserInfo;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;


@RestController
public class UserController {


    @Autowired
    private UserService userService;
    @Autowired
    private HoldingService holdingService;
    @GetMapping("/api/v1/users/{id}")
    public Result<UserInfo> getUserInfo(@PathVariable Long id) {
        return Result.success(userService.getUserInfo(id));
    }

    @GetMapping("/api/v1/users/{id}/holdings")
    public Result<List<Holding>> getUserHoldings(@PathVariable Long id) {
        List<Holding> holdings = holdingService.getUserHoldings(id);
        return Result.success(holdings);
    }

    @GetMapping("/api/v1/users/{id}/holdings/top-bottom")
    public Result<TopBottomHoldingsResponseDTO> getTopAndBottomHoldings(@PathVariable Long id) {
        return Result.success(holdingService.getTopAndBottomHoldings(id));
    }

    @GetMapping("/api/v1/users/{id}/kline/{symbol}")
    public Result<List<KLineCandleDTO>> getKLineData(@PathVariable Long id, @PathVariable String symbol) {
        return Result.success(holdingService.getKLineData(id, symbol));
    }

}
