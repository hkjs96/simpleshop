package com.example.simpleshop.domain.product;

import com.example.simpleshop.domain.user.User;
import com.example.simpleshop.domain.user.UserRepository;
import com.example.simpleshop.dto.product.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpSession;
import java.io.File;
import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    @Value("${file.upload-dir}")
    private String uploadDir;

    public ProductResponse create(ProductRequest req, MultipartFile image, HttpSession session) throws IOException {
        Long userId = (Long) session.getAttribute("USER_ID");
        User writer = userRepository.findById(userId).orElseThrow();

        String imageUrl = saveImage(image);

        Product product = Product.builder()
                .name(req.name())
                .description(req.description())
                .price(req.price())
                .imageUrl(imageUrl)
                .writer(writer)
                .build();

        productRepository.save(product);

        return toDto(product);
    }

    public List<ProductResponse> findAll() {
        return productRepository.findAll().stream().map(this::toDto).toList();
    }

    public ProductResponse findById(Long id) {
        return productRepository.findById(id).map(this::toDto).orElseThrow();
    }

    public void update(Long id, ProductUpdateRequest req, MultipartFile image, HttpSession session) throws IOException {
        Long userId = (Long) session.getAttribute("USER_ID");
        Product product = productRepository.findById(id).orElseThrow();

        if (!product.getWriter().getId().equals(userId)) {
            throw new IllegalStateException("작성자만 수정할 수 있습니다.");
        }

        String imageUrl = (image != null && !image.isEmpty()) ? saveImage(image) : product.getImageUrl();
        product.update(req.name(), req.description(), req.price(), imageUrl);
    }

    public void delete(Long id, HttpSession session) {
        Long userId = (Long) session.getAttribute("USER_ID");
        Product product = productRepository.findById(id).orElseThrow();

        if (!product.getWriter().getId().equals(userId)) {
            throw new IllegalStateException("작성자만 삭제할 수 있습니다.");
        }

        productRepository.delete(product);
    }

    private String saveImage(MultipartFile image) throws IOException {
        if (image == null || image.isEmpty()) return null;

        File dir = new File(uploadDir);
        if (!dir.exists()) dir.mkdirs();

        String filename = System.currentTimeMillis() + "_" + image.getOriginalFilename();
        File dest = new File(dir, filename);
        image.transferTo(dest);
        return "/images/" + filename;
    }

    private ProductResponse toDto(Product p) {
        return new ProductResponse(p.getId(), p.getName(), p.getDescription(), p.getPrice(), p.getImageUrl(), p.getWriter().getId());
    }
}
