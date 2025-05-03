package com.example.simpleshop.domain.product;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.Id;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProductImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String imageUrl;

    private int imageOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    @Builder
    public ProductImage(String imageUrl, int imageOrder, Product product) {
        this.imageUrl = imageUrl;
        this.imageOrder = imageOrder;
        this.product = product;
    }

    public void updateOrder(int order) {
        this.imageOrder = order;
    }
}
