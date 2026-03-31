package com.gongxifacai.gongxifacai.service.impl;

import com.gongxifacai.gongxifacai.common.CommonErrorCode;
import com.gongxifacai.gongxifacai.dto.FundTransactionResponseDTO;
import com.gongxifacai.gongxifacai.dto.PageResponseDTO;
import com.gongxifacai.gongxifacai.entity.FundTransaction;
import com.gongxifacai.gongxifacai.entity.User;
import com.gongxifacai.gongxifacai.exception.BusinessException;
import com.gongxifacai.gongxifacai.repository.FundTransactionRepository;
import com.gongxifacai.gongxifacai.repository.UserRepository;
import com.gongxifacai.gongxifacai.service.FundTransactionService;
import com.gongxifacai.gongxifacai.service.UserService;
import com.gongxifacai.gongxifacai.util.BigDecimalUtil;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FundTransactionServiceImpl implements FundTransactionService {

    private final FundTransactionRepository fundTransactionRepository;
    private final UserService userService;

    @Override
    public PageResponseDTO<FundTransactionResponseDTO> getUserFundTransactions(Long userId, int page, int size) {
        if (!userService.existsById(userId)) {
            throw new BusinessException(CommonErrorCode.NOT_FOUND);
        }
        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return PageResponseDTO.from(
                fundTransactionRepository.findByUser_Id(userId, pageable)
                        .map(FundTransactionResponseDTO::from)
        );
    }

    @Override
    @Transactional
    public FundTransaction processFundTransaction(Long userId, FundTransaction.FundTransactionType transactionType, BigDecimal amount, String description) {
        if (!BigDecimalUtil.isGreaterThanZero(amount)) {
            throw new BusinessException("金额必须大于0");
        }

        User user = userService.getUser(userId);

        if (transactionType == FundTransaction.FundTransactionType.DEPOSIT) {
            user.setAvailableCash(user.getAvailableCash().add(amount));
        } else if (transactionType == FundTransaction.FundTransactionType.WITHDRAW) {
            BigDecimal newAvailableCash = user.getAvailableCash().subtract(amount);
            if (BigDecimalUtil.isLessThanZero(newAvailableCash)) {
                throw new BusinessException("余额不足");
            }
            user.setAvailableCash(newAvailableCash);
        } else {
            throw new BusinessException("未知的交易类型");
        }

        userService.save(user);

        FundTransaction transaction = new FundTransaction();
        transaction.setUser(user);
        transaction.setTransactionType(transactionType);
        transaction.setTotalAmount(amount);
        transaction.setDescription(description);

        return fundTransactionRepository.save(transaction);
    }
}
