package com.example.simpleshop.domain.product;

import com.example.simpleshop.domain.common.S3ImageService;
import com.example.simpleshop.domain.user.User;
import com.example.simpleshop.domain.user.UserRepository;
import com.example.simpleshop.dto.product.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final S3ImageService s3ImageService;

    public ProductResponse create(ProductRequest req) {
        Long userId = getCurrentUserId();
        User writer = userRepository.findById(userId).orElseThrow();

        Product product = Product.builder()
                .name(req.name())
                .description(req.description())
                .price(req.price())
                .imageUrl(null) // 이미지 업로드는 별도 API에서 처리
                .writer(writer)
                .build();

        productRepository.save(product);
        return toDto(product);
    }

    public String updateImage(Long productId, MultipartFile image) throws IOException {
        Long userId = getCurrentUserId();
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new NoSuchElementException("존재하지 않는 상품입니다."));

        if (!product.getWriter().getId().equals(userId)) {
            throw new IllegalStateException("작성자만 수정할 수 있습니다.");
        }

        if (product.getImageUrl() != null) {
            s3ImageService.delete(product.getImageUrl());
        }

        String imageUrl = s3ImageService.upload(image);
        product.update(product.getName(), product.getDescription(), product.getPrice(), imageUrl);
        productRepository.save(product);

        return imageUrl;
    }

    public List<ProductResponse> findAll() {
        return productRepository.findAll().stream().map(this::toDto).toList();
    }

    public ProductResponse findById(Long id) {
        return productRepository.findById(id)
                .map(this::toDto)
                .orElseThrow(() -> new NoSuchElementException("해당 상품을 찾을 수 없습니다."));
    }

    public void update(Long id, ProductUpdateRequest req) {
        Long userId = getCurrentUserId();
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("존재하지 않는 상품입니다."));

        if (!product.getWriter().getId().equals(userId)) {
            throw new IllegalStateException("작성자만 수정할 수 있습니다.");
        }

        product.update(req.name(), req.description(), req.price(), product.getImageUrl());
        productRepository.save(product);
    }

    public void delete(Long id) {
        Long userId = getCurrentUserId();
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("존재하지 않는 상품입니다."));

        if (!product.getWriter().getId().equals(userId)) {
            throw new IllegalStateException("작성자만 삭제할 수 있습니다.");
        }

        if (product.getImageUrl() != null) {
            s3ImageService.delete(product.getImageUrl());
        }

        productRepository.delete(product);
    }

    private ProductResponse toDto(Product p) {
        boolean isLocalImage = false; // S3 기반이므로 false
        return new ProductResponse(p.getId(), p.getName(), p.getDescription(), p.getPrice(), p.getImageUrl(), isLocalImage, p.getWriter().getId());
    }

    private Long getCurrentUserId() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof User user) {
            return user.getId();
        } else {
            throw new IllegalStateException("로그인된 사용자를 확인할 수 없습니다.");
        }
    }
}
