package com.gongxifacai.gongxifacai.service.impl;

import com.gongxifacai.gongxifacai.common.CommonErrorCode;
import com.gongxifacai.gongxifacai.dto.FundTransactionResponseDTO;
import com.gongxifacai.gongxifacai.dto.PageResponseDTO;
import com.gongxifacai.gongxifacai.exception.BusinessException;
import com.gongxifacai.gongxifacai.repository.FundTransactionRepository;
import com.gongxifacai.gongxifacai.repository.UserRepository;
import com.gongxifacai.gongxifacai.service.FundTransactionService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FundTransactionServiceImpl implements FundTransactionService {

    private final FundTransactionRepository fundTransactionRepository;
    private final UserRepository userRepository;

    @Override
    public PageResponseDTO<FundTransactionResponseDTO> getUserFundTransactions(Long userId, int page, int size) {
        if (!userRepository.existsById(userId)) {
            throw new BusinessException(CommonErrorCode.NOT_FOUND);
        }
        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return PageResponseDTO.from(
                fundTransactionRepository.findByUser_Id(userId, pageable)
                        .map(FundTransactionResponseDTO::from)
        );
    }
}
