package com.example.simpleshop.dto.product;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.util.List;

@Schema(description = "상품 응답 DTO")
@Builder
public record ProductResponse(
        Long id,
        String name,
        String description,
        int price,
        List<ProductImageResponse> images, // ✅ 이미지 목록
        Long writerId
) {}
