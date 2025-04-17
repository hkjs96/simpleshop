package com.example.simpleshop.dto.product;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "상품 등록 요청 DTO")
public record ProductRequest(
        @Schema(description = "상품명", example = "귀여운 고양이 인형")
        @NotBlank String name,

        @Schema(description = "설명", example = "말랑한 촉감의 고양이 인형입니다.")
        String description,

        @Schema(description = "가격", example = "15000")
        @NotNull Integer price
) {}
