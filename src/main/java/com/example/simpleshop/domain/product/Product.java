package com.example.simpleshop.domain.product;

import com.example.simpleshop.domain.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

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

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sortOrder ASC") // ✅ 자동 정렬
    private List<ProductImage> images = new ArrayList<>();


    @ManyToOne(fetch = FetchType.LAZY)
    private User writer;

    @Builder
    public Product(String name, String description, int price, User writer) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.writer = writer;
    }

    public void update(String name, String description, int price) {
        this.name = name;
        this.description = description;
        this.price = price;
    }
}
