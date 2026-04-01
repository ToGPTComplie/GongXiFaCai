package com.gongxifacai.gongxifacai.controller;

import com.gongxifacai.gongxifacai.common.Result;
import com.gongxifacai.gongxifacai.dto.HoldingDTO;
import com.gongxifacai.gongxifacai.dto.KLineCandleDTO;
import com.gongxifacai.gongxifacai.dto.TopBottomHoldingsResponseDTO;
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

    @Test
    void getTopAndBottomHoldings_ReturnsThreeBestAndWorst() {
        TopBottomHoldingsResponseDTO responseDTO = new TopBottomHoldingsResponseDTO(
                List.of(createHolding("AAPL", "100"), createHolding("MSFT", "30"), createHolding("GOOGL", "5")),
                List.of(createHolding("NVDA", "-50"), createHolding("TSLA", "-10"), createHolding("GOOGL", "5"))
        );
        when(holdingService.getTopAndBottomHoldings(1L)).thenReturn(responseDTO);

        Result<TopBottomHoldingsResponseDTO> response = userController.getTopAndBottomHoldings(1L);

        assertEquals(200, response.getCode());
        assertEquals(3, response.getData().getTopProfitable().size());
        assertEquals(3, response.getData().getTopLosing().size());
        assertEquals("AAPL", response.getData().getTopProfitable().get(0).getTicker());
        assertEquals("MSFT", response.getData().getTopProfitable().get(1).getTicker());
        assertEquals("GOOGL", response.getData().getTopProfitable().get(2).getTicker());
        assertEquals("NVDA", response.getData().getTopLosing().get(0).getTicker());
        assertEquals("TSLA", response.getData().getTopLosing().get(1).getTicker());
        assertEquals("GOOGL", response.getData().getTopLosing().get(2).getTicker());
    }

    private HoldingDTO createHolding(String ticker, String pl) {
        HoldingDTO dto = new HoldingDTO();
        dto.setTicker(ticker);
        dto.setPl(new BigDecimal(pl));
        return dto;
    }
}
