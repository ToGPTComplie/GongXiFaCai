package com.gongxifacai.gongxifacai.controller;

import com.gongxifacai.gongxifacai.common.Result;
import com.gongxifacai.gongxifacai.entity.Holding;
import com.gongxifacai.gongxifacai.service.UserService;
import com.gongxifacai.gongxifacai.service.impl.UserServiceImpl.UserInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserService userService;

    private UserController userController;

    @BeforeEach
    void setUp() {
        userController = new UserController();
        ReflectionTestUtils.setField(userController, "userService", userService);
    }

    @Test
    void getUserInfo_ReturnsResultWrappedData() {
        UserInfo userInfo = new UserInfo(1L, "Alice", new BigDecimal("1000.00"));
        when(userService.getUserInfo(1L)).thenReturn(userInfo);

        Object response = userController.getUserInfo(1L);

        Result<?> result = assertInstanceOf(Result.class, response);
        assertEquals(200, result.getCode());
        assertEquals("Success", result.getMessage());
        assertEquals(userInfo, result.getData());
    }

    @Test
    void getUserHoldings_ReturnsValuationData() {
        Holding holding = new Holding();
        holding.setTicker("INTC");
        holding.setQuantity(new BigDecimal("10"));
        holding.setAverageCost(new BigDecimal("40"));
        List<Holding> holdings = List.of(holding);
        when(userService.getUserHoldings(1L)).thenReturn(holdings);

        UserController spyController = spy(userController);
        doReturn(new BigDecimal("41.19")).when(spyController).getCurrentPrice("INTC");
        Object response = spyController.getUserHoldings(1L);

        Result<?> result = assertInstanceOf(Result.class, response);
        assertEquals(200, result.getCode());
        assertEquals("Success", result.getMessage());
        UserController.HoldingValuation valuation = ((List<UserController.HoldingValuation>) result.getData()).getFirst();
        assertEquals(new BigDecimal("411.90"), valuation.marketValue());
        assertEquals(new BigDecimal("11.90"), valuation.profitLoss());
    }

    @Test
    void getCurrentPrice_UsesOneMinuteCache() {
        UserController controller = spy(userController);
        List<BigDecimal> callPrices = new ArrayList<>();
        callPrices.add(new BigDecimal("41.19"));
        callPrices.add(new BigDecimal("42.00"));

        doReturn(callPrices.getFirst())
                .doReturn(callPrices.get(1))
                .when(controller).fetchLatestPriceFromAlphaVantage("INTC");

        BigDecimal first = controller.getCurrentPrice("INTC");
        BigDecimal second = controller.getCurrentPrice("INTC");

        assertEquals(new BigDecimal("41.19"), first);
        assertEquals(new BigDecimal("41.19"), second);
    }

    @Test
    @SuppressWarnings("unchecked")
    void getPortfolioData_Returns50MarketRowsWithoutUserId() {
        UserController controller = spy(userController);
        Map<String, Object> quote = Map.of(
                "01. symbol", "AAPL",
                "05. price", "100.00",
                "09. change", "1.50",
                "10. change percent", "1.50%"
        );
        doReturn(quote).when(controller).fetchGlobalQuoteFromAlphaVantage(org.mockito.ArgumentMatchers.anyString());

        Object response = controller.getPortfolioData();

        Result<?> result = assertInstanceOf(Result.class, response);
        assertEquals(200, result.getCode());
        List<Map<String, Object>> data = (List<Map<String, Object>>) result.getData();
        assertEquals(50, data.size());
        verify(userService, never()).getUserHoldings(org.mockito.ArgumentMatchers.anyLong());
    }
}
