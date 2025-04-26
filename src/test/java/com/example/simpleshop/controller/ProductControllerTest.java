package com.example.simpleshop.controller;

import com.example.simpleshop.domain.user.UserRepository;
import com.example.simpleshop.dto.product.ImageDownloadResponse;
import com.example.simpleshop.dto.product.ProductResponse;
import com.example.simpleshop.dto.product.ProductResponseWithImage;
import com.example.simpleshop.domain.product.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.io.IOException;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductController.class)
public class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductService productService;

    @MockitoBean
    private UserRepository userRepository;

    private String base64ImageData;
    private ProductResponse defaultResponse;
    private ProductResponseWithImage responseWithImage;
    private List<ProductResponseWithImage> listWithImages;

    @BeforeEach
    void setUp() {
        base64ImageData = Base64.getEncoder().encodeToString("test image data".getBytes());

        defaultResponse = new ProductResponse(
                1L, "Test Product", "Test Description", 1000,
                "/images/test_image.jpg", true, 1L
        );

        responseWithImage = new ProductResponseWithImage(
                1L, "Test Product", "Test Description", 1000,
                "/images/test_image.jpg", true, 1L,
                base64ImageData, "image/jpeg"
        );

        listWithImages = Arrays.asList(
                new ProductResponseWithImage(
                        1L, "Test Product 1", "Test Description 1", 1000,
                        "/images/test_image1.jpg", true, 1L,
                        base64ImageData, "image/jpeg"
                ),
                new ProductResponseWithImage(
                        2L, "Test Product 2", "Test Description 2", 2000,
                        "/images/test_image2.jpg", true, 1L,
                        base64ImageData, "image/jpeg"
                )
        );
    }

    @Test
    @WithMockUser
    void findById_ShouldReturnProductResponse() throws Exception {
        when(productService.findById(anyLong())).thenReturn(defaultResponse);

        mockMvc.perform(get("/api/products/1")
                        .sessionAttr("USER_ID", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Test Product"))
                .andExpect(jsonPath("$.description").value("Test Description"))
                .andExpect(jsonPath("$.price").value(1000))
                .andExpect(jsonPath("$.imageUrl").value("/images/test_image.jpg"))
                .andExpect(jsonPath("$.isLocalImage").value(true))
                .andExpect(jsonPath("$.writerId").value(1));
    }

    @Test
    @WithMockUser
    void findByIdWithImage_ShouldReturnProductResponseWithImage() throws Exception {
        when(productService.findByIdWithImage(anyLong())).thenReturn(responseWithImage);

        mockMvc.perform(get("/api/products/1/with-image")
                        .sessionAttr("USER_ID", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Test Product"))
                .andExpect(jsonPath("$.description").value("Test Description"))
                .andExpect(jsonPath("$.price").value(1000))
                .andExpect(jsonPath("$.imageUrl").value("/images/test_image.jpg"))
                .andExpect(jsonPath("$.isLocalImage").value(true))
                .andExpect(jsonPath("$.writerId").value(1))
                .andExpect(jsonPath("$.imageData").value(base64ImageData))
                .andExpect(jsonPath("$.imageContentType").value("image/jpeg"));
    }

    @Test
    @WithMockUser
    void findAllWithImages_ShouldReturnListOfProductResponseWithImage() throws Exception {
        when(productService.findAllWithImages()).thenReturn(listWithImages);

        mockMvc.perform(get("/api/products/with-images")
                        .sessionAttr("USER_ID", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Test Product 1"))
                .andExpect(jsonPath("$[0].imageData").value(base64ImageData))
                .andExpect(jsonPath("$[0].imageContentType").value("image/jpeg"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].name").value("Test Product 2"))
                .andExpect(jsonPath("$[1].imageData").value(base64ImageData))
                .andExpect(jsonPath("$[1].imageContentType").value("image/jpeg"));
    }

    @Test
    @WithMockUser
    void downloadImage_ShouldReturnFileResource() throws Exception {
        byte[] imageData = "test image data".getBytes();
        Resource resource = new ByteArrayResource(imageData);
        ImageDownloadResponse downloadResponse = new ImageDownloadResponse(resource, "image/jpeg", "test_image.jpg");

        when(productService.getProductImage(anyLong())).thenReturn(downloadResponse);

        mockMvc.perform(get("/api/products/1/image")
                        .sessionAttr("USER_ID", 1L))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"test_image.jpg\""))
                .andExpect(content().contentType("image/jpeg"))
                .andExpect(content().bytes(imageData));
    }

    @Test
    @WithMockUser
    void downloadImage_NotFound_ShouldReturn404() throws Exception {
        when(productService.getProductImage(anyLong())).thenThrow(new IOException("File not found"));

        mockMvc.perform(get("/api/products/1/image")
                        .sessionAttr("USER_ID", 1L))
                .andExpect(status().isNotFound());
    }
}
