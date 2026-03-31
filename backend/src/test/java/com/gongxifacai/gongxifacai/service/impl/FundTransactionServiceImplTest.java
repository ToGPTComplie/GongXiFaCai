package com.gongxifacai.gongxifacai.service.impl;

import com.gongxifacai.gongxifacai.common.CommonErrorCode;
import com.gongxifacai.gongxifacai.dto.FundTransactionResponseDTO;
import com.gongxifacai.gongxifacai.dto.PageResponseDTO;
import com.gongxifacai.gongxifacai.entity.FundTransaction;
import com.gongxifacai.gongxifacai.entity.User;
import com.gongxifacai.gongxifacai.exception.BusinessException;
import com.gongxifacai.gongxifacai.repository.FundTransactionRepository;
import com.gongxifacai.gongxifacai.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FundTransactionServiceImplTest {

    @Mock
    private FundTransactionRepository fundTransactionRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private FundTransactionServiceImpl fundTransactionService;

    private User testUser;
    private FundTransaction testTransaction;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setName("Test User");
        testUser.setAvailableCash(new BigDecimal("1000.00"));

        testTransaction = new FundTransaction();
        testTransaction.setId(100L);
        testTransaction.setUser(testUser);
        testTransaction.setTransactionType(FundTransaction.FundTransactionType.DEPOSIT);
        testTransaction.setTotalAmount(new BigDecimal("500.00"));
        testTransaction.setDescription("Test Deposit");
        testTransaction.setCreatedAt(LocalDateTime.now());
    }

    @Test
    void getUserFundTransactions_UserExists_ReturnsPage() {
        // Arrange
        Long userId = 1L;
        int page = 0;
        int size = 10;
        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<FundTransaction> transactionPage = new PageImpl<>(List.of(testTransaction), pageable, 1);

        when(userRepository.existsById(userId)).thenReturn(true);
        when(fundTransactionRepository.findByUser_Id(eq(userId), any(PageRequest.class))).thenReturn(transactionPage);

        // Act
        PageResponseDTO<FundTransactionResponseDTO> response = fundTransactionService.getUserFundTransactions(userId, page, size);

        // Assert
        assertNotNull(response);
        assertEquals(1, response.getTotalElements());
        assertEquals(1, response.getContent().size());

        FundTransactionResponseDTO dto = response.getContent().get(0);
        assertEquals(testTransaction.getId(), dto.getId());
        assertEquals(testTransaction.getTotalAmount(), dto.getTotalAmount());
        assertEquals(testTransaction.getTransactionType(), dto.getTransactionType());
    }

    @Test
    void getUserFundTransactions_UserDoesNotExist_ThrowsBusinessException() {
        // Arrange
        Long userId = 999L;
        when(userRepository.existsById(userId)).thenReturn(false);

        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            fundTransactionService.getUserFundTransactions(userId, 0, 10);
        });

        assertEquals(CommonErrorCode.NOT_FOUND.getCode(), exception.getCode());
    }
}
