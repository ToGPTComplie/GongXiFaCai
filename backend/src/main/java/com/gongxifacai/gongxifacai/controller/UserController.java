package com.gongxifacai.gongxifacai.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gongxifacai.gongxifacai.common.CommonErrorCode;
import com.gongxifacai.gongxifacai.common.Result;
import com.gongxifacai.gongxifacai.entity.Holding;
import com.gongxifacai.gongxifacai.exception.BusinessException;
import com.gongxifacai.gongxifacai.service.UserService;
import com.gongxifacai.gongxifacai.dto.UserInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@RestController
public class UserController {
    private static final String API_KEY = "2FSNJP54LS7YWX1D";
    private static final long PRICE_CACHE_TTL_MILLIS = 60_000L;

    private static final List<String> DEFAULT_PORTFOLIO_SYMBOLS = List.of(
              "MSFT"
    );

    @Autowired
    private UserService userService;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Map<String, CachedPrice> priceCache = new ConcurrentHashMap<>();

    @GetMapping("/api/v1/users/{id}")
    public Result<UserInfo> getUserInfo(@PathVariable Long id) {
        return Result.success(userService.getUserInfo(id));
    }

    @GetMapping("/api/v1/users/{id}/holdings")
    public Result<List<HoldingValuation>> getUserHoldings(@PathVariable Long id) {
        List<Holding> holdings = userService.getUserHoldings(id);
        List<HoldingValuation> data = holdings.stream().map(this::toHoldingValuation).toList();
        return Result.success(data);
    }

    @GetMapping("/portfolio-data")
    public Result<List<JsonNode>> getPortfolioData() {
        List<JsonNode> data = new ArrayList<>(DEFAULT_PORTFOLIO_SYMBOLS.size());
        for (String symbol : DEFAULT_PORTFOLIO_SYMBOLS) {
            data.add(fetchGlobalQuoteFromAlphaVantage(symbol));
        }
        return Result.success(data);
    }

    private HoldingValuation toHoldingValuation(Holding holding) {
        BigDecimal currentPrice = getCurrentPrice(holding.getTicker());
        BigDecimal quantity = holding.getQuantity();
        BigDecimal averageCost = holding.getAverageCost();
        BigDecimal marketValue = currentPrice.multiply(quantity);
        BigDecimal profitLoss = currentPrice.subtract(averageCost).multiply(quantity);

        return new HoldingValuation(
                holding.getTicker(),
                quantity,
                averageCost,
                currentPrice,
                marketValue,
                profitLoss
        );
    }

    protected BigDecimal getCurrentPrice(String symbol) {
        long now = System.currentTimeMillis();
        CachedPrice cached = priceCache.get(symbol);
        if (cached != null && now - cached.fetchedAtMillis() < PRICE_CACHE_TTL_MILLIS) {
            return cached.price();
        }

        BigDecimal latestPrice = fetchLatestPriceFromAlphaVantage(symbol);
        priceCache.put(symbol, new CachedPrice(latestPrice, now));
        return latestPrice;
    }

    protected BigDecimal fetchLatestPriceFromAlphaVantage(String symbol) {
        JsonNode quote = fetchGlobalQuoteFromAlphaVantage(symbol);
        Object price = quote.get("05. price");
        if (price == null || price.toString().isBlank()) {
            throw new BusinessException(CommonErrorCode.SYSTEM_ERROR, "行情数据为空: " + symbol);
        }
        return new BigDecimal(price.toString());
    }

    protected JsonNode fetchGlobalQuoteFromAlphaVantage(String symbol) {
        String urlStr = "https://www.alphavantage.co/query?function=GLOBAL_QUOTE&symbol=" + symbol + "&apikey=" + API_KEY;
        System.out.println(urlStr);
        try {
            HttpURLConnection conn = (HttpURLConnection) new URL(urlStr).openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");

            int code = conn.getResponseCode();
            InputStream is = (code >= 200 && code < 300) ? conn.getInputStream() : conn.getErrorStream();
            String response;
            try (BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                response = br.lines().collect(Collectors.joining("\n"));
            } finally {
                conn.disconnect();
            }

            JsonNode quote = objectMapper.readTree(response).path("Global Quote");
            System.out.println("quote:"+response);


            return quote;
        } catch (IOException e) {
            throw new BusinessException(CommonErrorCode.SYSTEM_ERROR, "行情服务调用失败: " + e.getMessage());
        }
    }

    private record CachedPrice(BigDecimal price, long fetchedAtMillis) {
    }

    public record HoldingValuation(
            String ticker,
            BigDecimal quantity,
            BigDecimal averageCost,
            BigDecimal currentPrice,
            BigDecimal marketValue,
            BigDecimal profitLoss
    ) {
    }
}
