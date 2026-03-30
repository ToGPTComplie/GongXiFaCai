package com.gongxifacai.gongxifacai.service;

import com.gongxifacai.gongxifacai.dto.PageResponseDTO;
import com.gongxifacai.gongxifacai.dto.TradeTransactionResponseDTO;

public interface TradeTransactionService {
    PageResponseDTO<TradeTransactionResponseDTO> getUserTradeTransactions(Long userId, int page, int size);
}
