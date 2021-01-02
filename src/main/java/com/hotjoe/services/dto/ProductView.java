package com.hotjoe.services.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.hotjoe.services.model.Product;

public class ProductView {
    @JsonProperty
    public final String productCode;
    @JsonProperty
    public final String productName;
    @JsonProperty
    public final String productScale;
    @JsonProperty
    public final String productVendor;
    @JsonProperty
    public final String productDescription;
    @JsonProperty
    public final Short quantityInStock;
    @JsonProperty
    public final Double buyPrice;
    @JsonProperty
    public final Double MSRP;

    public ProductView(Product product) {
        this.productCode = product.getProductCode();
        this.productName = product.getProductName();
        this.productScale = product.getProductScale();
        this.productVendor = product.getProductVendor();
        this.productDescription = product.getProductDescription();
        this.quantityInStock = product.getQuantityInStock();
        this.buyPrice = product.getBuyPrice();
        this.MSRP = product.getMSRP();
    }
}
