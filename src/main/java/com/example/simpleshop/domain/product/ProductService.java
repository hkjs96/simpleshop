package com.example.simpleshop.domain.product;

import com.example.simpleshop.domain.common.S3ImageService;
import com.example.simpleshop.domain.user.User;
import com.example.simpleshop.domain.user.UserRepository;
import com.example.simpleshop.dto.product.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpSession;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final S3ImageService s3ImageService;

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
            s3ImageService.delete(product.getImageUrl());
        }

        String imageUrl = s3ImageService.upload(image);
        product.update(product.getName(), product.getDescription(), product.getPrice(), imageUrl);

        // 변경된 엔티티를 저장
        productRepository.save(product);
    }

    public void deleteImage(Long productId, HttpSession session) {
        Long userId = (Long) session.getAttribute("USER_ID");
        Product product = productRepository.findById(productId).orElseThrow();

        if (!product.getWriter().getId().equals(userId)) {
            throw new IllegalStateException("작성자만 삭제할 수 있습니다.");
        }

        if (product.getImageUrl() != null) {
            s3ImageService.delete(product.getImageUrl());
            product.update(product.getName(), product.getDescription(), product.getPrice(), null);
            productRepository.save(product);
        }
    }

    public List<ProductResponse> findAll() {
        return productRepository.findAll().stream().map(this::toDto).toList();
    }

    public ProductResponse findById(Long id) {
        return productRepository.findById(id).map(this::toDto).orElseThrow();
    }

    /**
     * 모든 상품을 조회하고 이미지 데이터를 Base64로 인코딩하여 포함한 응답을 반환합니다.
     * 프론트엔드 애플리케이션에서 별도의 이미지 요청 없이 상품 정보와 이미지를 함께 표시할 수 있습니다.
     * 
     * @return 이미지 데이터가 포함된 상품 목록
     */
    public List<ProductResponseWithImage> findAllWithImages() {
        List<Product> products = productRepository.findAll();
        List<ProductResponseWithImage> result = new ArrayList<>();
        
        for (Product product : products) {
            ProductResponse baseResponse = toDto(product);
            try {
                if (product.getImageUrl() != null && !product.getImageUrl().isBlank()) {
                    // 이미지 데이터와 콘텐츠 타입 가져오기
                    ImageData imageData = getImageData(product);
                    result.add(ProductResponseWithImage.fromProductResponse(
                            baseResponse, 
                            imageData.base64Data(), 
                            imageData.contentType()
                    ));
                } else {
                    // 이미지가 없는 경우
                    result.add(ProductResponseWithImage.fromProductResponse(
                            baseResponse, null, null
                    ));
                }
            } catch (IOException e) {
                // 이미지 처리 중 오류 발생 시 이미지 없이 반환
                result.add(ProductResponseWithImage.fromProductResponse(
                        baseResponse, null, null
                ));
            }
        }
        
        return result;
    }

    /**
     * 특정 상품을 조회하고 이미지 데이터를 Base64로 인코딩하여 포함한 응답을 반환합니다.
     * 
     * @param id 상품 ID
     * @return 이미지 데이터가 포함된 상품 정보
     */
    public ProductResponseWithImage findByIdWithImage(Long id) {
        Product product = productRepository.findById(id).orElseThrow();
        ProductResponse baseResponse = toDto(product);
        
        try {
            if (product.getImageUrl() != null && !product.getImageUrl().isBlank()) {
                // 이미지 데이터와 콘텐츠 타입 가져오기
                ImageData imageData = getImageData(product);
                return ProductResponseWithImage.fromProductResponse(
                        baseResponse, 
                        imageData.base64Data(), 
                        imageData.contentType()
                );
            }
        } catch (IOException e) {
            // 이미지 처리 중 오류 발생 시 이미지 없이 반환
        }
        
        // 이미지가 없거나 오류 발생 시
        return ProductResponseWithImage.fromProductResponse(baseResponse, null, null);
    }

    /**
     * 상품의 이미지 데이터를 Base64로 인코딩하여 반환하는 내부 메서드
     * 
     * @param product 상품 엔티티
     * @return 이미지 데이터와 콘텐츠 타입을 포함한 레코드
     * @throws IOException 이미지 파일 처리 중 오류 발생 시
     */
    private ImageData getImageData(Product product) throws IOException {
        if (product.getImageUrl() == null || product.getImageUrl().isBlank()) {
            return new ImageData(null, null);
        }
        
        // "/images/filename.jpg" → "filename.jpg"
        String filename = product.getImageUrl().substring(product.getImageUrl().lastIndexOf("/") + 1);
        File imageFile = new File(getUploadDir(), filename);
        
        if (!imageFile.exists()) {
            return new ImageData(null, null);
        }
        
        // 이미지 파일의 MIME 타입 확인
        String contentType = Files.probeContentType(imageFile.toPath());
        if (contentType == null) {
            contentType = "application/octet-stream";
        }
        
        // 이미지 파일을 바이트 배열로 읽고 Base64로 인코딩
        byte[] fileContent = Files.readAllBytes(imageFile.toPath());
        String base64Data = Base64.getEncoder().encodeToString(fileContent);
        
        return new ImageData(base64Data, contentType);
    }

    /**
     * 이미지 데이터와 콘텐츠 타입을 포함하는 내부 레코드
     */
    private record ImageData(String base64Data, String contentType) {}

    public ImageDownloadResponse getProductImage(Long id) throws IOException {
        Product product = productRepository.findById(id).orElseThrow();
        
        if (product.getImageUrl() == null || product.getImageUrl().isBlank()) {
            throw new IllegalStateException("이미지가 없습니다.");
        }
        
        // "/images/filename.jpg" → "filename.jpg"
        String filename = product.getImageUrl().substring(product.getImageUrl().lastIndexOf("/") + 1);
        File imageFile = new File(getUploadDir(), filename);
        
        if (!imageFile.exists()) {
            throw new IllegalStateException("이미지 파일을 찾을 수 없습니다.");
        }
        
        Resource resource = new FileSystemResource(imageFile);
        String contentType = Files.probeContentType(imageFile.toPath());
        if (contentType == null) {
            contentType = "application/octet-stream";
        }
        
        return new ImageDownloadResponse(resource, contentType, filename);
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
        // 모든 이미지는 로컬에 저장되므로 isLocalImage는 항상 true
        // 만약 클라우드 스토리지를 사용하는 경우 여기서 구분 로직을 추가할 수 있음
        boolean isLocalImage = p.getImageUrl() != null && !p.getImageUrl().isBlank();
        return new ProductResponse(p.getId(), p.getName(), p.getDescription(), p.getPrice(), p.getImageUrl(), isLocalImage, p.getWriter().getId());
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