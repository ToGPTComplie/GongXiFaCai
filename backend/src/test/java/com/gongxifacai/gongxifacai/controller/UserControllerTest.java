package com.gongxifacai.gongxifacai.controller;

import com.gongxifacai.gongxifacai.common.Result;
import com.gongxifacai.gongxifacai.dto.KLineCandleDTO;
import com.gongxifacai.gongxifacai.entity.Holding;
import com.gongxifacai.gongxifacai.service.HoldingService;
import com.gongxifacai.gongxifacai.service.UserService;
import com.gongxifacai.gongxifacai.dto.UserInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private HoldingService holdingService;

    private UserController userController;

    @BeforeEach
    void setUp() {
        userController = new UserController();
        ReflectionTestUtils.setField(userController, "userService", userService);
        ReflectionTestUtils.setField(userController, "holdingService", holdingService);
    }

    @Test
    void getUserInfo_ReturnsResultWrappedData() {
        UserInfo userInfo = new UserInfo(1L, "Alice", new BigDecimal("1000.00"));
        when(userService.getUserInfo(1L)).thenReturn(userInfo);

        Result<UserInfo> response = userController.getUserInfo(1L);

        assertEquals(200, response.getCode());
        assertEquals("Success", response.getMessage());
        assertEquals(userInfo, response.getData());
    }

    @Test
    void getUserHoldings_ReturnsList() {
        Holding holding = new Holding();
        holding.setTicker("AAPL");
        holding.setQuantity(new BigDecimal("10"));
        holding.setAverageCost(new BigDecimal("150.00"));
        List<Holding> holdings = List.of(holding);
        when(holdingService.getUserHoldings(1L)).thenReturn(holdings);

        Result<List<Holding>> response = userController.getUserHoldings(1L);

        assertEquals(200, response.getCode());
        assertEquals(1, response.getData().size());
        assertEquals("AAPL", response.getData().getFirst().getTicker());
    }

    @Test
    void getKLineData_DelegatesToHoldingService() {
        KLineCandleDTO candle = new KLineCandleDTO(
                1700000000L, 1700000000000L,
                new BigDecimal("150.00"), new BigDecimal("155.00"),
                new BigDecimal("149.00"), new BigDecimal("153.00"),
                1000000L
        );
        when(holdingService.getKLineData(1L, "AAPL")).thenReturn(List.of(candle));

        Result<List<KLineCandleDTO>> response = userController.getKLineData(1L, "AAPL");

        assertEquals(200, response.getCode());
        assertEquals(1, response.getData().size());
        assertEquals(new BigDecimal("153.00"), response.getData().getFirst().getClose());
    }
}
