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
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
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
    void getUserHoldings_ReturnsResultWrappedData() {
        Holding holding = new Holding();
        holding.setTicker("600519");
        holding.setQuantity(new BigDecimal("10.0000"));
        holding.setAverageCost(new BigDecimal("100.0000"));
        List<Holding> holdings = List.of(holding);
        when(userService.getUserHoldings(1L)).thenReturn(holdings);

        Object response = userController.getUserHoldings(1L);

        Result<?> result = assertInstanceOf(Result.class, response);
        assertEquals(200, result.getCode());
        assertEquals("Success", result.getMessage());
        assertEquals(holdings, result.getData());
    }
}
