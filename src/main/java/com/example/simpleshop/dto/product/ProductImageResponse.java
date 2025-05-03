package com.example.simpleshop.dto.product;

import lombok.Builder;

@Builder
public record ProductImageResponse(
        Long id,
        String url,
        int order
) {}
