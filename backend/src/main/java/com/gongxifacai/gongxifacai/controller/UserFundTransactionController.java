package com.gongxifacai.gongxifacai.controller;

import com.gongxifacai.gongxifacai.common.Result;
import com.gongxifacai.gongxifacai.dto.FundTransactionRequestDTO;
import com.gongxifacai.gongxifacai.dto.FundTransactionResponseDTO;
import com.gongxifacai.gongxifacai.dto.PageResponseDTO;
import com.gongxifacai.gongxifacai.entity.FundTransaction;
import com.gongxifacai.gongxifacai.service.FundTransactionService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserFundTransactionController {

    private final FundTransactionService fundTransactionService;

    @GetMapping("/{id}/fund-transactions")
    public Result<PageResponseDTO<FundTransactionResponseDTO>> getFundTransactions(
            @PathVariable("id") Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.success(fundTransactionService.getUserFundTransactions(userId, page, size));
    }

    @PostMapping("/{id}/fund-transactions")
    //@PreAuthorize("#userId == authentication.principal.id")
    //demo没有登录
    public Result<FundTransactionResponseDTO> processFundTransaction(
            @PathVariable("id") Long userId,
            @Valid @RequestBody FundTransactionRequestDTO request) {
        FundTransaction transaction = fundTransactionService.processFundTransaction(
                userId,
                request.getTransactionType(),
                request.getAmount(),
                request.getDescription()
        );
        return Result.success(FundTransactionResponseDTO.from(transaction));
    }
}

