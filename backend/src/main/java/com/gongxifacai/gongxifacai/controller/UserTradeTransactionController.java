package com.gongxifacai.gongxifacai.controller;

import com.gongxifacai.gongxifacai.common.Result;
import com.gongxifacai.gongxifacai.dto.PageResponseDTO;
import com.gongxifacai.gongxifacai.dto.TradeTransactionResponseDTO;
import com.gongxifacai.gongxifacai.service.TradeTransactionService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
public class UserTradeTransactionController {

    private final TradeTransactionService tradeTransactionService;

    public UserTradeTransactionController(TradeTransactionService tradeTransactionService) {
        this.tradeTransactionService = tradeTransactionService;
    }

    @GetMapping("/{id}/trade-transactions")
    public Result<PageResponseDTO<TradeTransactionResponseDTO>> getTradeTransactions(
            @PathVariable("id") Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.success(tradeTransactionService.getUserTradeTransactions(userId, page, size));
    }
}
