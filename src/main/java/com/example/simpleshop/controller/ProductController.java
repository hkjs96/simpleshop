package com.example.simpleshop.controller;

import com.example.simpleshop.domain.product.ProductService;
import com.example.simpleshop.dto.product.*;
import com.example.simpleshop.dto.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Tag(name = "상품 API", description = "상품 등록/조회/수정/삭제")
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @Operation(summary = "상품 등록 (정보만)")
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<ProductResponse>> create(@RequestBody ProductRequest request) {
        return ResponseEntity.ok(ApiResponse.success(productService.create(request)));
    }

    @Operation(summary = "상품 이미지 업로드 (S3)")
    @PostMapping(value = "/{productId}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<String>> uploadImage(
            @PathVariable Long productId,
            @RequestPart("image") MultipartFile image
    ) throws IOException {
        String imageUrl = productService.updateImage(productId, image);
        return ResponseEntity.ok(ApiResponse.success(imageUrl));
    }

    @Operation(summary = "상품 목록 조회")
    @GetMapping
    public ResponseEntity<ApiResponse<List<ProductResponse>>> findAll() {
        return ResponseEntity.ok(ApiResponse.success(productService.findAll()));
    }

    @Operation(summary = "상품 상세 조회")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductResponse>> findById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(productService.findById(id)));
    }

    @Operation(summary = "상품 수정")
    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<String>> update(
            @PathVariable Long id,
            @RequestBody ProductUpdateRequest request) {
        productService.update(id, request);
        return ResponseEntity.ok(ApiResponse.success("수정 완료"));
    }

    @Operation(summary = "상품 삭제")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> delete(@PathVariable Long id) {
        productService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("삭제 완료"));
    }
}
