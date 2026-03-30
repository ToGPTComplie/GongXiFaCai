package com.gongxifacai.gongxifacai.service;

import com.gongxifacai.gongxifacai.common.CommonErrorCode;
import com.gongxifacai.gongxifacai.dto.TradeTransactionResponseDTO;
import com.gongxifacai.gongxifacai.exception.BusinessException;
import com.gongxifacai.gongxifacai.repository.TradeTransactionRepository;
import com.gongxifacai.gongxifacai.repository.UserRepository;
import com.gongxifacai.gongxifacai.dto.PageResponseDTO;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
public class TradeTransactionService {

    private final TradeTransactionRepository tradeTransactionRepository;
    private final UserRepository userRepository;

    public TradeTransactionService(TradeTransactionRepository tradeTransactionRepository, UserRepository userRepository) {
        this.tradeTransactionRepository = tradeTransactionRepository;
        this.userRepository = userRepository;
    }

    public PageResponseDTO<TradeTransactionResponseDTO> getUserTradeTransactions(Long userId, int page, int size) {
        if (!userRepository.existsById(userId)) {
            throw new BusinessException(CommonErrorCode.NOT_FOUND, "用户不存在");
        }
        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return PageResponseDTO.from(
                tradeTransactionRepository.findByUser_Id(userId, pageable)
                        .map(TradeTransactionResponseDTO::from)
        );
    }
}
