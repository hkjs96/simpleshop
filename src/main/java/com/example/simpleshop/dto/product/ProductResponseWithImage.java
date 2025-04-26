package com.example.simpleshop.dto.product;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "상품 응답 DTO (이미지 데이터 포함)")
public record ProductResponseWithImage(
        @Schema(description = "상품 ID") Long id,
        @Schema(description = "상품명") String name,
        @Schema(description = "설명") String description,
        @Schema(description = "가격") int price,
        @Schema(description = "이미지 URL") String imageUrl,
        @Schema(description = "이미지가 로컬에 저장되어 있는지 여부") boolean isLocalImage,
        @Schema(description = "작성자 ID") Long writerId,
        @Schema(description = "Base64로 인코딩된 이미지 데이터") String imageData,
        @Schema(description = "이미지 MIME 타입") String imageContentType
) {
    // Convert from ProductResponse and add image data
    public static ProductResponseWithImage fromProductResponse(ProductResponse response, String imageData, String imageContentType) {
        return new ProductResponseWithImage(
                response.id(),
                response.name(),
                response.description(),
                response.price(),
                response.imageUrl(),
                response.isLocalImage(),
                response.writerId(),
                imageData,
                imageContentType
        );
    }
}