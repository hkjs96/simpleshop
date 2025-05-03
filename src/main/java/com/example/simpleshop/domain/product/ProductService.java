package com.example.simpleshop.domain.product;

import com.example.simpleshop.domain.common.S3ImageService;
import com.example.simpleshop.domain.user.User;
import com.example.simpleshop.domain.user.UserRepository;
import com.example.simpleshop.dto.product.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final S3ImageService s3ImageService;

    @Transactional
    public ProductResponse create(ProductRequest req) {
        Long userId = getCurrentUserId();
        User writer = userRepository.findById(userId).orElseThrow();

        Product product = Product.builder()
                .name(req.name())
                .description(req.description())
                .price(req.price())
                .writer(writer)
                .build();

        productRepository.save(product);
        return toDto(product);
    }

    @Transactional
    public List<String> updateImages(Long productId, List<MultipartFile> images) throws IOException {
        Long userId = getCurrentUserId();

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new NoSuchElementException("존재하지 않는 상품입니다."));

        if (!product.getWriter().getId().equals(userId)) {
            throw new IllegalStateException("작성자만 수정할 수 있습니다.");
        }

        // 기존 이미지 모두 삭제 (S3 및 DB)
        for (ProductImage image : product.getImages()) {
            s3ImageService.delete(image.getImageUrl());
        }
        product.getImages().clear();

        // 새로운 이미지 업로드 및 순서 부여
        List<String> uploadedUrls = new ArrayList<>();
        for (int i = 0; i < images.size(); i++) {
            String imageUrl = s3ImageService.upload(images.get(i));
            ProductImage image = ProductImage.builder()
                    .imageUrl(imageUrl)
                    .imageOrder(i)
                    .product(product)
                    .build();

            product.getImages().add(image);
            uploadedUrls.add(imageUrl);
        }

        productRepository.save(product);
        return uploadedUrls;
    }



    @Transactional(readOnly = true)
    public Page<ProductResponse> findAll(Pageable pageable, String sortBy) {
        Sort sort;
        switch (sortBy) {
            case "priceAsc" -> sort = Sort.by(Sort.Direction.ASC, "price");
            case "priceDesc" -> sort = Sort.by(Sort.Direction.DESC, "price");
            default -> sort = Sort.by(Sort.Direction.DESC, "id"); // 최신순
        }
        pageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);

        return productRepository.findAll(pageable)
                .map(this::toDto);
    }

    @Transactional(readOnly = true)
    public ProductResponse findById(Long id) {
        return productRepository.findById(id)
                .map(this::toDto)
                .orElseThrow(() -> new NoSuchElementException("해당 상품을 찾을 수 없습니다."));
    }

    @Transactional
    public void update(Long id, ProductUpdateRequest req) {
        Long userId = getCurrentUserId();
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("존재하지 않는 상품입니다."));

        if (!product.getWriter().getId().equals(userId)) {
            throw new IllegalStateException("작성자만 수정할 수 있습니다.");
        }

        product.update(req.name(), req.description(), req.price());
        productRepository.save(product);
    }

    @Transactional
    public void delete(Long id) {
        Long userId = getCurrentUserId();
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("존재하지 않는 상품입니다."));

        if (!product.getWriter().getId().equals(userId)) {
            throw new IllegalStateException("작성자만 삭제할 수 있습니다.");
        }

        if (!product.getImages().isEmpty()) {
            s3ImageService.imageDelete(product.getImages());
        }

        productRepository.delete(product);
    }

    @Transactional
    public void deleteImage(Long productId, Long imageId) {
        Long userId = getCurrentUserId();

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new NoSuchElementException("존재하지 않는 상품입니다."));

        if (!product.getWriter().getId().equals(userId)) {
            throw new IllegalStateException("작성자만 삭제할 수 있습니다.");
        }

        List<ProductImage> images = product.getImages();

        // 삭제 대상 찾기
        ProductImage target = images.stream()
                .filter(img -> img.getId().equals(imageId))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("이미지를 찾을 수 없습니다."));

        // S3 삭제 + 리스트에서 제거
        s3ImageService.delete(target.getImageUrl());
        images.remove(target);

        // ✅ 순서 재정렬
        for (int i = 0; i < images.size(); i++) {
            images.get(i).updateOrder(i);
        }

        productRepository.save(product);
    }



    private ProductResponse toDto(Product p) {
        List<ProductImageResponse> imageDtos = p.getImages().stream()
                .sorted(Comparator.comparingInt(ProductImage::getImageOrder)) // ✅ 순서 정렬
                .map(img -> ProductImageResponse.builder()
                        .id(img.getId())
                        .url(img.getImageUrl())
                        .order(img.getImageOrder())
                        .build()
                )
                .toList();

        return ProductResponse.builder()
                .id(p.getId())
                .name(p.getName())
                .description(p.getDescription())
                .price(p.getPrice())
                .images(imageDtos)
                .writerId(p.getWriter().getId())
                .build();
    }



    private Long getCurrentUserId() {
        // SecurityContext 기반 인증 처리 필요 (구현 생략)
        return 1L; // 테스트용 사용자 ID
    }
}
