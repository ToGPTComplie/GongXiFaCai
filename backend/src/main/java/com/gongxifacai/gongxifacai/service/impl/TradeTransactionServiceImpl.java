package com.gongxifacai.gongxifacai.service.impl;

import com.gongxifacai.gongxifacai.common.CommonErrorCode;
import com.gongxifacai.gongxifacai.dto.PageResponseDTO;
import com.gongxifacai.gongxifacai.dto.TradeTransactionResponseDTO;
import com.gongxifacai.gongxifacai.entity.Holding;
import com.gongxifacai.gongxifacai.entity.TradeTransaction;
import com.gongxifacai.gongxifacai.entity.User;
import com.gongxifacai.gongxifacai.exception.BusinessException;
import com.gongxifacai.gongxifacai.repository.HoldingRepository;
import com.gongxifacai.gongxifacai.repository.TradeTransactionRepository;
import com.gongxifacai.gongxifacai.repository.UserRepository;
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
    private final UserRepository userRepository;
    private final HoldingRepository holdingRepository;
    private final UserService userService;
    private final HoldingService holdingService;

    @Override
    public PageResponseDTO<TradeTransactionResponseDTO> getUserTradeTransactions(Long userId, int page, int size) {
        if (!userRepository.existsById(userId)) {
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

        BigDecimal totalAmount = quantity.multiply(price);

        Holding holding;

        if(transactionType == TradeTransaction.TransactionType.BUY){

            BigDecimal balance = user.getAvailableCash().subtract(totalAmount);

            // 余额不足
            if(BigDecimalUtil.isLessThanZero(balance)){
                throw new BusinessException("余额不足");
            }

            // 扣减现金
            user.setAvailableCash(balance);

            holding = holdingService.getOrCreateHolding(userId,ticker,assetType);

            //计算平均持仓成本
            BigDecimal oldQuantity = holding.getQuantity();
            BigDecimal newQuantity = oldQuantity.add(quantity);
            BigDecimal newTotalAmount = holding.getAverageCost().multiply(oldQuantity).add(totalAmount) ;
            BigDecimal newAverageCost = newTotalAmount.divide(newQuantity, 4, RoundingMode.HALF_UP);

            holding.setQuantity(newQuantity);
            holding.setAverageCost(newAverageCost);

        } else if (transactionType == TradeTransaction.TransactionType.SELL) {

            holding = holdingService.getHolding(userId, ticker);

            BigDecimal oldQuantity = holding.getQuantity();
            BigDecimal newQuantity = oldQuantity.subtract(quantity);

            //持仓不足
            if(BigDecimalUtil.isLessThanZero(newQuantity)){
                throw new BusinessException("持仓不足");
            }

            BigDecimal balance = user.getAvailableCash().add(totalAmount);
            user.setAvailableCash(balance);

            holding.setQuantity(newQuantity);

        }
        else {
            throw new BusinessException("未知的交易类型");
        }

        // 保存状态

        userRepository.save(user);
        holdingRepository.save(holding);

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
