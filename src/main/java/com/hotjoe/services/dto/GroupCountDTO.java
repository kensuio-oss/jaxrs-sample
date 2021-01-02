package com.hotjoe.services.dto;

import com.hotjoe.services.model.Product;

public class GroupCountDTO {
    private ProductView product;
    private Long cnt;

    public GroupCountDTO(Product product, Long cnt) {
        this.product = new ProductView(product);
        this.cnt = cnt;
    }

    public ProductView getProduct() {
        return product;
    }

    public void setProduct(ProductView product) {
        this.product = product;
    }

    public Long getCnt() {
        return cnt;
    }

    public void setCnt(Long cnt) {
        this.cnt = cnt;
    }
}
