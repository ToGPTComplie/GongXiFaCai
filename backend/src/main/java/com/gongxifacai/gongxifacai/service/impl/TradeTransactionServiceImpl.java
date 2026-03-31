package com.gongxifacai.gongxifacai.service.impl;

import com.gongxifacai.gongxifacai.common.CommonErrorCode;
import com.gongxifacai.gongxifacai.dto.PageResponseDTO;
import com.gongxifacai.gongxifacai.dto.TradeTransactionResponseDTO;
import com.gongxifacai.gongxifacai.entity.Holding;
import com.gongxifacai.gongxifacai.entity.TradeTransaction;
import com.gongxifacai.gongxifacai.entity.User;
import com.gongxifacai.gongxifacai.exception.BusinessException;
import com.gongxifacai.gongxifacai.repository.TradeTransactionRepository;
import com.gongxifacai.gongxifacai.service.HoldingService;
import com.gongxifacai.gongxifacai.service.TradeTransactionService;
import com.gongxifacai.gongxifacai.service.UserService;
import com.gongxifacai.gongxifacai.util.BigDecimalUtil;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TradeTransactionServiceImpl implements TradeTransactionService {

    private final TradeTransactionRepository tradeTransactionRepository;
    private final UserService userService;
    private final HoldingService holdingService;

    @Override
    public PageResponseDTO<TradeTransactionResponseDTO> getUserTradeTransactions(Long userId, int page, int size) {
        if (!userService.existsById(userId)) {
            throw new BusinessException(CommonErrorCode.NOT_FOUND);
        }
        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return PageResponseDTO.from(
                tradeTransactionRepository.findByUser_Id(userId, pageable)
                        .map(TradeTransactionResponseDTO::from)
        );
    }

    @Override
    @Transactional
    public TradeTransaction processTrade(Long userId, String ticker, Holding.AssetType assetType, TradeTransaction.TransactionType transactionType, BigDecimal quantity, BigDecimal price) {

        if (!BigDecimalUtil.isGreaterThanZero(quantity) || !BigDecimalUtil.isGreaterThanZero(price)) {
            throw new BusinessException(CommonErrorCode.BAD_REQUEST, "交易数量和价格必须大于0");
        }
        // 获取用户
        User user = userService.getUser(userId);

        // 交易总额
        BigDecimal totalAmount = quantity.multiply(price);

        if (transactionType == TradeTransaction.TransactionType.BUY) {

            BigDecimal balance = user.getAvailableCash().subtract(totalAmount);

            // 余额不足
            if (BigDecimalUtil.isLessThanZero(balance)) {
                throw new BusinessException("余额不足");
            }

            // 扣减现金
            user.setAvailableCash(balance);

            holdingService.applyBuy(userId, ticker, assetType, quantity, totalAmount);

        } else if (transactionType == TradeTransaction.TransactionType.SELL) {

            BigDecimal balance = user.getAvailableCash().add(totalAmount);
            user.setAvailableCash(balance);

            holdingService.applySell(userId, ticker, quantity);
        } else {
            throw new BusinessException("未知的交易类型");
        }

        // 保存状态

        userService.save(user);

        TradeTransaction tradeTransaction = new TradeTransaction();

        tradeTransaction.setUser(user);
        tradeTransaction.setTicker(ticker);
        tradeTransaction.setAssetType(assetType);
        tradeTransaction.setTransactionType(transactionType);
        tradeTransaction.setQuantity(quantity);
        tradeTransaction.setPrice(price);
        tradeTransaction.setTotalAmount(totalAmount);

        // 记录并返回流水。
        return tradeTransactionRepository.save(tradeTransaction);
    }
}
