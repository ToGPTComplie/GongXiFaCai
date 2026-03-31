package com.gongxifacai.gongxifacai.controller;

import com.gongxifacai.gongxifacai.common.Result;
import com.gongxifacai.gongxifacai.dto.PageResponseDTO;
import com.gongxifacai.gongxifacai.dto.TradeTransactionRequestDTO;
import com.gongxifacai.gongxifacai.dto.TradeTransactionResponseDTO;
import com.gongxifacai.gongxifacai.entity.TradeTransaction;
import com.gongxifacai.gongxifacai.service.TradeTransactionService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserTradeTransactionController {

    private final TradeTransactionService tradeTransactionService;

    @GetMapping("/{id}/trade-transactions")
    public Result<PageResponseDTO<TradeTransactionResponseDTO>> getTradeTransactions(
            @PathVariable("id") Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.success(tradeTransactionService.getUserTradeTransactions(userId, page, size));
    }

    @PostMapping("/{id}/trade-transactions")
    //@PreAuthorize("#userId == authentication.principal.id")
    //demo没有登录
    public Result<TradeTransactionResponseDTO> processTrade(
            @PathVariable("id") Long userId,
            @Valid @RequestBody TradeTransactionRequestDTO request) {
        TradeTransaction transaction = tradeTransactionService.processTrade(
                userId,
                request.getTicker(),
                request.getAssetType(),
                request.getTransactionType(),
                request.getQuantity(),
                request.getPrice()
        );
        return Result.success(TradeTransactionResponseDTO.from(transaction));
    }

}
