package com.gongxifacai.gongxifacai.controller;
import com.gongxifacai.gongxifacai.common.CommonErrorCode;
import com.gongxifacai.gongxifacai.common.Result;
import com.gongxifacai.gongxifacai.dto.HoldingDTO;
import com.gongxifacai.gongxifacai.dto.KLineCandleDTO;
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
    // 这就是你问的 base-url
    public static final String BASE_URL = "https://query1.finance.yahoo.com";

    @Autowired
    private UserService userService;
    @Autowired
    private HoldingService holdingService;
    @GetMapping("/api/v1/users/{id}")
    public Result<UserInfo> getUserInfo(@PathVariable Long id) {
        return Result.success(userService.getUserInfo(id));
    }
    @GetMapping("test")
    public void getProtofeil  () throws IOException, InterruptedException {
        String ticker="AAPL";
        try {
            String url = BASE_URL + "/v8/finance/chart/" + ticker + "?interval=1d&range=1d";
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("User-Agent", "Mozilla/5.0")
                    .build();

            HttpResponse<String> response =
                    HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new BusinessException(CommonErrorCode.SYSTEM_ERROR, "Failed to fetch market price");
            }

            JsonObject root = JsonParser.parseString(response.body()).getAsJsonObject();
            System.out.println(root);
            JsonObject chart = root.getAsJsonObject("chart");
            JsonArray result = chart == null ? null : chart.getAsJsonArray("result");
            if (result == null || result.isEmpty()) {
                throw new BusinessException(CommonErrorCode.NOT_FOUND, "Market data not found: " + ticker);
            }

            JsonObject first = result.get(0).getAsJsonObject();
            JsonObject meta = first.getAsJsonObject("meta");
            JsonElement priceElement = meta == null ? null : meta.get("regularMarketPrice");
            if (priceElement == null || priceElement.isJsonNull()) {
                throw new BusinessException(CommonErrorCode.NOT_FOUND, "regularMarketPrice not found: " + ticker);
            }
            System.out.println(meta);
            System.out.println(BigDecimal.valueOf(priceElement.getAsDouble()));
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new BusinessException(CommonErrorCode.SYSTEM_ERROR, "Failed to fetch market price");
        }
    }
//    @GetMapping("/api/v1/users/{id}/holdings")
//    public Result<List<Holding>> getUserHoldings(@PathVariable Long id) {
//        List<Holding> holdings = holdingService.getUserHoldings(id);
//        return Result.success(holdings);
//    }

    @GetMapping("/api/v1/users/{id}/{symbol}")
    public Result<List<KLineCandleDTO>> getKLineData(@PathVariable Long id, @PathVariable String symbol) {
        return Result.success(holdingService.getKLineData(id, symbol));
    }

}
