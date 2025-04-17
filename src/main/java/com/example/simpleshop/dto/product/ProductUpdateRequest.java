package com.example.simpleshop.dto.product;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "상품 수정 요청 DTO")
public record ProductUpdateRequest(
        @Schema(description = "상품명", example = "고양이 인형 V2")
        String name,

        @Schema(description = "설명", example = "더 말랑한 촉감입니다.")
        String description,

        @Schema(description = "가격", example = "17000")
        Integer price
) {}
