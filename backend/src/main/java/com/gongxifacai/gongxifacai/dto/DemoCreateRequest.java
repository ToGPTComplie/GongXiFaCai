package com.gongxifacai.gongxifacai.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class DemoCreateRequest {
    @Schema(description = "资源名称", example = "示例名称")
    @NotBlank(message = "资源名称不能为空")
    private String name;

    @Schema(description = "资源数量", example = "100")
    @NotNull(message = "资源数量不能为空")
    @Positive(message = "资源数量必须为正数")
    private Integer amount;
}
