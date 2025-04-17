package com.example.simpleshop.controller;

import com.example.simpleshop.domain.product.ProductService;
import com.example.simpleshop.dto.product.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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

    @Operation(summary = "상품 등록")
    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<ProductResponse> create(
            @RequestPart("info") @Valid ProductRequest request,
            @RequestPart(value = "image", required = false) MultipartFile image,
            HttpSession session) throws IOException {
        return ResponseEntity.ok(productService.create(request, image, session));
    }

    @Operation(summary = "상품 목록 조회")
    @GetMapping
    public ResponseEntity<List<ProductResponse>> findAll() {
        return ResponseEntity.ok(productService.findAll());
    }

    @Operation(summary = "상품 상세 조회")
    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(productService.findById(id));
    }

    @Operation(summary = "상품 수정")
    @PutMapping(value = "/{id}", consumes = "multipart/form-data")
    public ResponseEntity<String> update(
            @PathVariable Long id,
            @RequestPart("info") @Valid ProductUpdateRequest request,
            @RequestPart(value = "image", required = false) MultipartFile image,
            HttpSession session) throws IOException {
        productService.update(id, request, image, session);
        return ResponseEntity.ok("수정 완료");
    }

    @Operation(summary = "상품 삭제")
    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable Long id, HttpSession session) {
        productService.delete(id, session);
        return ResponseEntity.ok("삭제 완료");
    }
}
