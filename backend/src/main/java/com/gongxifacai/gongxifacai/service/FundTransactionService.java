package com.gongxifacai.gongxifacai.service;

import com.gongxifacai.gongxifacai.dto.FundTransactionResponseDTO;
import com.gongxifacai.gongxifacai.dto.PageResponseDTO;

public interface FundTransactionService {
    PageResponseDTO<FundTransactionResponseDTO> getUserFundTransactions(Long userId, int page, int size);
}
