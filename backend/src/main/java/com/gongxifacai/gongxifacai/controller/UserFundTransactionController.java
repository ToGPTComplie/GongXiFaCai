package com.gongxifacai.gongxifacai.controller;

import com.gongxifacai.gongxifacai.common.Result;
import com.gongxifacai.gongxifacai.dto.FundTransactionResponseDTO;
import com.gongxifacai.gongxifacai.dto.PageResponseDTO;
import com.gongxifacai.gongxifacai.service.FundTransactionService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
public class UserFundTransactionController {

    private final FundTransactionService fundTransactionService;

    public UserFundTransactionController(FundTransactionService fundTransactionService) {
        this.fundTransactionService = fundTransactionService;
    }

    @GetMapping("/{id}/fund-transactions")
    public Result<PageResponseDTO<FundTransactionResponseDTO>> getFundTransactions(
            @PathVariable("id") Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.success(fundTransactionService.getUserFundTransactions(userId, page, size));
    }
}

