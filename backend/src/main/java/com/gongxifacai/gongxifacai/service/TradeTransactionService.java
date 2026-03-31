package com.gongxifacai.gongxifacai.service;

import com.gongxifacai.gongxifacai.dto.PageResponseDTO;
import com.gongxifacai.gongxifacai.dto.TradeTransactionResponseDTO;
import com.gongxifacai.gongxifacai.entity.Holding;
import com.gongxifacai.gongxifacai.entity.TradeTransaction;

import java.math.BigDecimal;

public interface TradeTransactionService {
    PageResponseDTO<TradeTransactionResponseDTO> getUserTradeTransactions(Long userId, int page, int size);

    /**
     * 处理资产的买卖交易
     * @return 保存后的 TradeTransaction 流水记录
     */
    TradeTransaction processTrade(Long userId, String ticker, Holding.AssetType assetType,
                                  TradeTransaction.TransactionType transactionType,
                                  BigDecimal quantity, BigDecimal price);
}
