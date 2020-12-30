package com.hotjoe.services.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import javax.persistence.GeneratedValue;
 
@Entity
@Table(name="products")
public class Product {
    @Id
    @Column(name = "productCode")
    @GeneratedValue()
    private String productCode;

    @Column(name = "productName")
    private String productName;

    @ManyToOne()
    @JoinColumn(name = "productLine")
    private ProductLine productLine;
    @Column(name = "productScale")
    private String productScale;
    @Column(name = "productVendor")
    private String productVendor;
    @Column(name = "productDescription")
    private String productDescription;

    @Column(name = "quantityInStock")
    private Short quantityInStock;
    @Column(name = "buyPrice")
    private Double buyPrice;
    @Column(name = "MSRP")
    private Double MSRP;


    
    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public ProductLine getProductLine() {
        return productLine;
    }

    public void setProductLine(ProductLine productLine) {
        this.productLine = productLine;
    }

    public String getProductScale() {
        return productScale;
    }

    public void setProductScale(String productScale) {
        this.productScale = productScale;
    }

    public String getProductVendor() {
        return productVendor;
    }

    public void setProductVendor(String productVendor) {
        this.productVendor = productVendor;
    }

    public String getProductDescription() {
        return productDescription;
    }

    public void setProductDescription(String productDescription) {
        this.productDescription = productDescription;
    }

    public Short getQuantityInStock() {
        return quantityInStock;
    }

    public void setQuantityInStock(Short quantityInStock) {
        this.quantityInStock = quantityInStock;
    }

    public Double getBuyPrice() {
        return buyPrice;
    }

    public void setBuyPrice(Double buyPrice) {
        this.buyPrice = buyPrice;
    }

    public Double getMSRP() {
        return MSRP;
    }

    public void setMSRP(Double mSRP) {
        MSRP = mSRP;
    }

    public String getProductCode() {
        return productCode;
    }
}