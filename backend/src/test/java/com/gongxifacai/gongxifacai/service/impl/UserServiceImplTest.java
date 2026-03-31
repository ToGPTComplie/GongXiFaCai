package com.gongxifacai.gongxifacai.service.impl;

import com.gongxifacai.gongxifacai.entity.Holding;
import com.gongxifacai.gongxifacai.entity.User;
import com.gongxifacai.gongxifacai.repository.HoldingRepository;
import com.gongxifacai.gongxifacai.exception.BusinessException;
import com.gongxifacai.gongxifacai.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import com.gongxifacai.gongxifacai.dto.UserInfo;

import static com.gongxifacai.gongxifacai.common.CommonErrorCode.USER_NOT_FOUND;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private HoldingRepository holdingRepository;

    @InjectMocks
    private UserServiceImpl userService;

    private User mockUser;

    @BeforeEach
    void setUp() {
        mockUser = new User();
        mockUser.setId(1L);
        mockUser.setName("TestUser");
        mockUser.setAvailableCash(new BigDecimal("1000.00"));
    }

    @Test
    void getUser_Success() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));

        // Act
        User result = userService.getUser(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("TestUser", result.getName());
        assertEquals(new BigDecimal("1000.00"), result.getAvailableCash());
        verify(userRepository, times(1)).findById(1L);
    }

    @Test
    void getUser_NullId_ThrowsException() {
        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class, () -> userService.getUser(null));
        assertEquals(USER_NOT_FOUND.getCode(), exception.getCode());
        verify(userRepository, never()).findById(any());
    }

    @Test
    void getUser_NotFound_ThrowsException() {
        // Arrange
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class, () -> userService.getUser(99L));
        assertEquals(USER_NOT_FOUND.getCode(), exception.getCode());
        verify(userRepository, times(1)).findById(99L);
    }

    @Test
    void getUserInfo_Success() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));

        // Act
        UserInfo result = userService.getUserInfo(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals("TestUser", result.name());
        assertEquals(new BigDecimal("1000.00"), result.availableCash());
        verify(userRepository, times(1)).findById(1L);
    }

    @Test
    void getUserHoldings_Success() {
        // Arrange
        Holding holding = new Holding();
        holding.setUser(mockUser);
        holding.setTicker("600519");
        holding.setQuantity(new BigDecimal("10.0000"));
        holding.setAverageCost(new BigDecimal("100.0000"));
        List<Holding> holdings = List.of(holding);

        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
        when(holdingRepository.findByUserId(1L)).thenReturn(holdings);

        // Act
        List<Holding> result = userService.getUserHoldings(1L);

        // Assert
        assertEquals(1, result.size());
        assertEquals("600519", result.getFirst().getTicker());
        verify(userRepository, times(1)).findById(1L);
        verify(holdingRepository, times(1)).findByUserId(1L);
    }

    @Test
    void createUser_ValidInput_Success() {
        // Arrange
        String name = "NewUser";
        BigDecimal initialCash = new BigDecimal("500.00");

        User savedUser = new User();
        savedUser.setId(2L);
        savedUser.setName(name);
        savedUser.setAvailableCash(initialCash);

        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        // Act
        User result = userService.createUser(name, initialCash);

        // Assert
        assertNotNull(result);
        assertEquals(2L, result.getId());
        assertEquals("NewUser", result.getName());
        assertEquals(new BigDecimal("500.00"), result.getAvailableCash());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void createUser_NullNameAndNegativeCash_UsesDefaults() {
        // Arrange
        User savedUser = new User();
        savedUser.setId(3L);
        savedUser.setName("");
        savedUser.setAvailableCash(BigDecimal.ZERO);

        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        // Act
        User result = userService.createUser(null, new BigDecimal("-100.00"));

        // Assert
        assertNotNull(result);
        assertEquals("", result.getName());
        assertEquals(BigDecimal.ZERO, result.getAvailableCash());

        // Verify what was actually passed to save
        verify(userRepository).save(argThat(user ->
            "".equals(user.getName()) &&
            BigDecimal.ZERO.equals(user.getAvailableCash())
        ));
    }
}
