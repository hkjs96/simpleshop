package com.example.simpleshop.controller;

import com.example.simpleshop.domain.product.ProductService;
import com.example.simpleshop.dto.product.*;
import com.example.simpleshop.dto.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

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

    @Operation(summary = "상품 목록 조회 (페이징)")
    @GetMapping
    public ResponseEntity<ApiResponse<Page<ProductResponse>>> findAll(
            @Parameter(description = "페이지 번호 (0부터 시작)", example = "0")
            @RequestParam(value = "page", defaultValue = "0") int page,

            @Parameter(description = "페이지 크기 (1~100 사이 권장)", example = "10")
            @RequestParam(value = "size", defaultValue = "10") int size,

            @Parameter(description = "정렬 기준 (latest | priceAsc | priceDesc)", example = "latest")
            @RequestParam(defaultValue = "latest") String sortBy
    ) {
        if (page < 0 || size <= 0 || size > 100) {
            throw new IllegalArgumentException("page는 0 이상, size는 1~100 사이여야 합니다.");
        }

        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(ApiResponse.success(productService.findAll(pageable, sortBy)));
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
