package com.gongxifacai.gongxifacai.service.impl;

import com.gongxifacai.gongxifacai.common.CommonErrorCode;
import com.gongxifacai.gongxifacai.dto.HoldingDTO;
import com.gongxifacai.gongxifacai.dto.KLineCandleDTO;
import com.gongxifacai.gongxifacai.entity.Holding;
import com.gongxifacai.gongxifacai.exception.BusinessException;
import com.gongxifacai.gongxifacai.repository.HoldingRepository;
import com.gongxifacai.gongxifacai.service.HoldingService;
import com.gongxifacai.gongxifacai.service.UserService;
import com.gongxifacai.gongxifacai.util.BigDecimalUtil;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collector;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HoldingServiceImpl implements HoldingService {

    private static final String BASE_URL = "https://query1.finance.yahoo.com";

    private final HoldingRepository holdingRepository;
    private final UserService userService;

    //调用API获取实时价格
    @Override
    public BigDecimal getMarketPrice(String ticker) {
        try {
            String url = BASE_URL + "/v8/finance/chart/" + ticker + "?interval=1d&range=1d";
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("User-Agent", "Mozilla/5.0")
                    .build();

            HttpResponse<String> response =
                    HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println(response);
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new BusinessException(CommonErrorCode.SYSTEM_ERROR, "Failed to fetch market price");
            }

            JsonObject root = JsonParser.parseString(response.body()).getAsJsonObject();
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

            return BigDecimal.valueOf(priceElement.getAsDouble());
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new BusinessException(CommonErrorCode.SYSTEM_ERROR, "Failed to fetch market price");
        }
    }

    @Override
    public List<KLineCandleDTO> getKLineData(Long userId, String symbol) {
        if (!userService.existsById(userId)) {
            throw new BusinessException(CommonErrorCode.USER_NOT_FOUND);
        }

        try {
            String url = BASE_URL + "/v8/finance/chart/" + symbol + "?interval=1d&range=6mo";
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("User-Agent", "Mozilla/5.0")
                    .build();

            HttpResponse<String> response =
                    HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new BusinessException(CommonErrorCode.SYSTEM_ERROR, "Failed to fetch kline data");
            }

            JsonObject root = JsonParser.parseString(response.body()).getAsJsonObject();
            JsonObject chart = root.getAsJsonObject("chart");
            JsonArray result = chart == null ? null : chart.getAsJsonArray("result");
            if (result == null || result.isEmpty()) {
                throw new BusinessException(CommonErrorCode.NOT_FOUND, "Kline data not found: " + symbol);
            }

            JsonObject first = result.get(0).getAsJsonObject();
            JsonArray timestamps = first.getAsJsonArray("timestamp");
            JsonObject indicators = first.getAsJsonObject("indicators");
            JsonArray quoteArray = indicators == null ? null : indicators.getAsJsonArray("quote");
            JsonObject quote = (quoteArray == null || quoteArray.isEmpty()) ? null : quoteArray.get(0).getAsJsonObject();
            if (timestamps == null || quote == null) {
                throw new BusinessException(CommonErrorCode.NOT_FOUND, "Kline data not found: " + symbol);
            }

            JsonArray opens = quote.getAsJsonArray("open");
            JsonArray highs = quote.getAsJsonArray("high");
            JsonArray lows = quote.getAsJsonArray("low");
            JsonArray closes = quote.getAsJsonArray("close");
            JsonArray volumes = quote.getAsJsonArray("volume");

            int size = timestamps.size();
            List<KLineCandleDTO> candles = new ArrayList<>(size);
            for (int i = 0; i < size; i++) {
                Long timestamp = getLong(timestamps, i);
                candles.add(new KLineCandleDTO(
                        timestamp,
                        timestamp == null ? null : timestamp * 1000,
                        getBigDecimal(opens, i),
                        getBigDecimal(highs, i),
                        getBigDecimal(lows, i),
                        getBigDecimal(closes, i),
                        getLong(volumes, i)
                ));
            }
            return candles;
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new BusinessException(CommonErrorCode.SYSTEM_ERROR, "Failed to fetch kline data");
        }
    }

    //    获取market value和PL
    @Override
    public List<HoldingDTO> getUserHoldingsWithprice(Long userId) {
        try {
            List<Holding> holdings = holdingRepository.findByUser_Id(userId);
            System.out.println(holdings.get(0).getTicker());
            if (holdings == null || holdings.isEmpty()) {
                throw new BusinessException(CommonErrorCode.NOT_FOUND, "Holding not found");
            }

            List<HoldingDTO> result = new ArrayList<>();
            for (Holding holding : holdings) {
                HoldingDTO dto = new HoldingDTO();
                dto.setId(holding.getId());
                dto.setTicker(holding.getTicker());
                dto.setAssetType(holding.getAssetType());
                dto.setQuantity(holding.getQuantity());
                dto.setAverageCost(holding.getAverageCost());

                BigDecimal marketPrice = getMarketPrice(holding.getTicker());
                BigDecimal marketValue = marketPrice.multiply(holding.getQuantity());
                BigDecimal pl = marketPrice.subtract(holding.getAverageCost())
                        .multiply(holding.getQuantity());
                dto.setMarketValue(marketValue);
                dto.setPl(pl);
                result.add(dto);
            }
            return result;
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new BusinessException(CommonErrorCode.SYSTEM_ERROR, "Failed to load holdings valuation");
        }

    }


    @Override
    public List<Holding> getUserHoldings(Long userId) {
        return holdingRepository.findByUser_Id(userId);
    }


    @Override
    public Holding getHolding(Long userId, String ticker) {
        List<Holding> holdings = getUserHoldings(userId);

        for (Holding holding : holdings) {
            if (holding.getTicker().equals(ticker)) {
                return holding;
            }
        }
        throw new BusinessException(CommonErrorCode.NOT_FOUND, "持仓不存在");
    }

    @Override
    @Transactional
    public Holding getOrCreateHolding(Long userId, String ticker, Holding.AssetType assetType) {
        List<Holding> holdings = getUserHoldings(userId);
        for (Holding holding : holdings) {
            if (holding.getTicker().equals(ticker)) {
                return holding;
            }
        }

        Holding holding = new Holding();
        holding.setUser(userService.getReferenceById(userId));
        holding.setTicker(ticker);
        holding.setAssetType(assetType);
        holding.setQuantity(BigDecimal.ZERO);
        holding.setAverageCost(BigDecimal.ZERO);
        return holdingRepository.save(holding);
    }

    @Override
    public Map<Holding, BigDecimal> getMarketPricesByHolding(List<Holding> holdings) {
        return holdings.stream().
                collect(Collectors.toMap(h -> h, h -> this.getMarketPrice(h.getTicker())));
    }

    @Override
    public Map<String, BigDecimal> getMarketPricesByTicker(List<String> tickers) {
        return tickers.stream().
                collect(Collectors.toMap(t -> t, this::getMarketPrice));
    }

    @Override
    @Transactional
    public Holding applyBuy(Long userId, String ticker, Holding.AssetType assetType, BigDecimal quantity, BigDecimal totalAmount) {
        Holding holding = getOrCreateHolding(userId, ticker, assetType);
        BigDecimal oldQuantity = holding.getQuantity();
        BigDecimal newQuantity = oldQuantity.add(quantity);
        BigDecimal newTotalAmount = holding.getAverageCost().multiply(oldQuantity).add(totalAmount);
        BigDecimal newAverageCost = newTotalAmount.divide(newQuantity, 4, RoundingMode.HALF_UP);

        holding.setQuantity(newQuantity);
        holding.setAverageCost(newAverageCost);
        return holdingRepository.save(holding);
    }

    @Override
    @Transactional
    public Holding applySell(Long userId, String ticker, BigDecimal quantity) {
        Holding holding = getHolding(userId, ticker);
        BigDecimal oldQuantity = holding.getQuantity();
        BigDecimal newQuantity = oldQuantity.subtract(quantity);
        if (BigDecimalUtil.isLessThanZero(newQuantity)) {
            throw new BusinessException("Insufficient holding quantity");
        }

        holding.setQuantity(newQuantity);
        return holdingRepository.save(holding);
    }

    @Override
    public BigDecimal calculateHoldingMarketValue(Holding holding) {
        return getMarketPrice(holding.getTicker()).multiply(holding.getQuantity());
    }

    @Override
    public BigDecimal calculateTotalHoldingsMarketValue(List<Holding> holdings) {
        return holdings.stream().map(this::calculateHoldingMarketValue).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    public BigDecimal calculateTotalHoldingsMarketValue(Map<Holding, BigDecimal> currentMarketPriceMap) {
        return currentMarketPriceMap.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
    }


    public BigDecimal calculateRealizedPnl(Long userId, String ticker, BigDecimal quantity, BigDecimal price) {
        Holding holding = getHolding(userId, ticker);
        return quantity.multiply(price.subtract(holding.getAverageCost()));
    }

    private BigDecimal getBigDecimal(JsonArray array, int index) {
        if (array == null || index >= array.size()) {
            return null;
        }
        JsonElement element = array.get(index);
        if (element == null || element.isJsonNull()) {
            return null;
        }
        return BigDecimal.valueOf(element.getAsDouble());
    }

    private Long getLong(JsonArray array, int index) {
        if (array == null || index >= array.size()) {
            return null;
        }
        JsonElement element = array.get(index);
        if (element == null || element.isJsonNull()) {
            return null;
        }
        return element.getAsLong();
    }
}
