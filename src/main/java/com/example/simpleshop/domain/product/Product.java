package com.example.simpleshop.domain.product;

import com.example.simpleshop.domain.user.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Product {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String description;

    private int price;

    private String imageUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    private User writer;

    @Builder
    public Product(String name, String description, int price, String imageUrl, User writer) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.imageUrl = imageUrl;
        this.writer = writer;
    }

    public void update(String name, String description, int price, String imageUrl) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.imageUrl = imageUrl;
    }
}
