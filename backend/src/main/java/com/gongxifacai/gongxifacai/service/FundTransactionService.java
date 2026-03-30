package com.gongxifacai.gongxifacai.service;

import com.gongxifacai.gongxifacai.common.CommonErrorCode;
import com.gongxifacai.gongxifacai.dto.FundTransactionResponseDTO;
import com.gongxifacai.gongxifacai.dto.PageResponseDTO;
import com.gongxifacai.gongxifacai.exception.BusinessException;
import com.gongxifacai.gongxifacai.repository.FundTransactionRepository;
import com.gongxifacai.gongxifacai.repository.UserRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
public class FundTransactionService {

    private final FundTransactionRepository fundTransactionRepository;
    private final UserRepository userRepository;

    public FundTransactionService(FundTransactionRepository fundTransactionRepository, UserRepository userRepository) {
        this.fundTransactionRepository = fundTransactionRepository;
        this.userRepository = userRepository;
    }

    public PageResponseDTO<FundTransactionResponseDTO> getUserFundTransactions(Long userId, int page, int size) {
        if (!userRepository.existsById(userId)) {
            throw new BusinessException(CommonErrorCode.NOT_FOUND, "User not found");
        }
        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return PageResponseDTO.from(
                fundTransactionRepository.findByUser_Id(userId, pageable)
                        .map(FundTransactionResponseDTO::from)
        );
    }
}


