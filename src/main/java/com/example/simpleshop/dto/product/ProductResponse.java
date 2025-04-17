package com.example.simpleshop.dto.product;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "상품 응답 DTO")
public record ProductResponse(
        @Schema(description = "상품 ID") Long id,
        @Schema(description = "상품명") String name,
        @Schema(description = "설명") String description,
        @Schema(description = "가격") int price,
        @Schema(description = "이미지 URL") String imageUrl,
        @Schema(description = "작성자 ID") Long writerId
) {}
