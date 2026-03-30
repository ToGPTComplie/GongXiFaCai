package com.gongxifacai.gongxifacai.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.gongxifacai.gongxifacai.entity.FundTransaction;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class FundTransactionResponseDTO {

    private Long id;
    private Long userId;
    private FundTransaction.FundTransactionType transactionType;
    private BigDecimal totalAmount;
    private String description;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createdAt;

    public static FundTransactionResponseDTO from(FundTransaction t) {
        FundTransactionResponseDTO dto = new FundTransactionResponseDTO();
        dto.setId(t.getId());
        dto.setUserId(t.getUser().getId());
        dto.setTransactionType(t.getTransactionType());
        dto.setTotalAmount(t.getTotalAmount());
        dto.setDescription(t.getDescription());
        dto.setCreatedAt(t.getCreatedAt());
        return dto;
    }
}

