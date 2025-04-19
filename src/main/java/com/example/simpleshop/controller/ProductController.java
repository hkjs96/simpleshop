package com.example.simpleshop.controller;

import com.example.simpleshop.domain.product.ProductService;
import com.example.simpleshop.dto.product.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
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
    public ResponseEntity<ProductResponse> create(@Valid @RequestBody ProductRequest request, HttpSession session) {
        return ResponseEntity.ok(productService.create(request, session));
    }

    @Operation(summary = "상품 이미지 업로드")
    @PostMapping(value = "/{productId}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> uploadImage(
            @PathVariable Long productId,
            @RequestPart("image") MultipartFile image,
            HttpSession session
    ) throws IOException {
        productService.updateImage(productId, image, session);
        return ResponseEntity.ok("이미지 업로드 성공 (기존 이미지가 있다면 삭제됨)");
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
    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> update(
            @PathVariable Long id,
            @RequestBody @Valid ProductUpdateRequest request,
            HttpSession session) {
        productService.update(id, request, session);
        return ResponseEntity.ok("수정 완료");
    }

    @Operation(summary = "상품 삭제")
    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable Long id, HttpSession session) {
        productService.delete(id, session);
        return ResponseEntity.ok("삭제 완료");
    }
}
