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
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    @Value("${file.upload-dir}")
    private String uploadDir;

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

    public void updateImage(Long productId, MultipartFile image, HttpSession session) throws IOException {
        Long userId = (Long) session.getAttribute("USER_ID");
        Product product = productRepository.findById(productId).orElseThrow();

        if (!product.getWriter().getId().equals(userId)) {
            throw new IllegalStateException("작성자만 수정할 수 있습니다.");
        }

        if (product.getImageUrl() != null) {
            deleteFileFromDisk(product.getImageUrl());
        }

        String imageUrl = saveImage(image);
        product.update(product.getName(), product.getDescription(), product.getPrice(), imageUrl);


        // 변경사항을 즉시 데이터베이스에 반영
        /*
            flush() 메서드는 현재 영속성 컨텍스트의 변경 사항을 데이터베이스에 즉시 반영합니다. 이렇게 하면 트랜잭션이 커밋되기 전에도 업데이트가 데이터베이스에 반영됩니다.
        */
//        productRepository.flush();

        // 변경된 엔티티를 저장
        productRepository.save(product);
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

        File dir = getUploadDir();

        String filename = System.currentTimeMillis() + "_" + image.getOriginalFilename();
        File dest = new File(dir, filename);
        image.transferTo(dest);
        return "/images/" + filename;
    }

    private ProductResponse toDto(Product p) {
        return new ProductResponse(p.getId(), p.getName(), p.getDescription(), p.getPrice(), p.getImageUrl(), p.getWriter().getId());
    }

    private void deleteFileFromDisk(String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank()) return;

        // "/images/filename.jpg" → "filename.jpg"
        String filename = imageUrl.substring(imageUrl.lastIndexOf("/") + 1);
        File file = new File(uploadDir, filename);

        if (file.exists()) {
            boolean deleted = file.delete();
            if (!deleted) {
                System.err.println("[이미지 삭제 실패] 파일: " + file.getAbsolutePath());
            }
        }
    }

    private File getUploadDir() {
        // 상대 경로일 경우, 현재 실행 위치 기준으로 바꿔줌
        File dir = new File(uploadDir);
        if (!dir.isAbsolute()) {
            dir = new File(System.getProperty("user.dir"), uploadDir);
        }
        if (!dir.exists()) dir.mkdirs();
        return dir;
    }

    // 🔄 확장 고려: 다중 이미지 업로드 (선택적 구현)
    // 현재 구조는 상품 1개당 이미지 1개 기준입니다.
    // 다중 이미지 지원 시: ProductImage 엔티티 추가 + OneToMany 구성 + 별도 이미지 테이블 설계 필요
    // 또는 여러 번의 API 호출로 이미지 순차 업로드 방식 권장
}
