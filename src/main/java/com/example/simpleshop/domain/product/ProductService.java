package com.example.simpleshop.domain.product;

import com.example.simpleshop.domain.common.S3ImageService;
import com.example.simpleshop.domain.user.User;
import com.example.simpleshop.domain.user.UserRepository;
import com.example.simpleshop.dto.product.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final S3ImageService s3ImageService;

    public ProductResponse create(ProductRequest req, HttpSession session) {
        Long userId = (Long) session.getAttribute("USER_ID");
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

    public String updateImage(Long productId, MultipartFile image, HttpSession session) throws IOException {
        Long userId = (Long) session.getAttribute("USER_ID");
        Product product = productRepository.findById(productId).orElseThrow();

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
        return productRepository.findById(id).map(this::toDto).orElseThrow();
    }

    public void update(Long id, ProductUpdateRequest req, HttpSession session) {
        Long userId = (Long) session.getAttribute("USER_ID");
        Product product = productRepository.findById(id).orElseThrow();

        if (!product.getWriter().getId().equals(userId)) {
            throw new IllegalStateException("작성자만 수정할 수 있습니다.");
        }

        product.update(req.name(), req.description(), req.price(), product.getImageUrl());
        productRepository.save(product);
    }

    public void delete(Long id, HttpSession session) {
        Long userId = (Long) session.getAttribute("USER_ID");
        Product product = productRepository.findById(id).orElseThrow();

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
}
