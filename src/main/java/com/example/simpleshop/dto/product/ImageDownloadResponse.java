package com.example.simpleshop.dto.product;

import org.springframework.core.io.Resource;

/**
 * DTO for handling image download responses
 */
public record ImageDownloadResponse(
        Resource resource,
        String contentType,
        String filename
) {}