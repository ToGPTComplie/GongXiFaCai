package com.gongxifacai.gongxifacai.service;

import com.gongxifacai.gongxifacai.dto.FundTransactionResponseDTO;
import com.gongxifacai.gongxifacai.dto.PageResponseDTO;
import com.gongxifacai.gongxifacai.entity.FundTransaction;

import java.math.BigDecimal;

public interface FundTransactionService {
    PageResponseDTO<FundTransactionResponseDTO> getUserFundTransactions(Long userId, int page, int size);

    /**
     * 处理资金的出入金（充值/提现）
     * @return 保存后的 FundTransaction 流水记录
     */
    FundTransaction processFundTransaction(Long userId, FundTransaction.FundTransactionType transactionType, BigDecimal amount, String description);

}
