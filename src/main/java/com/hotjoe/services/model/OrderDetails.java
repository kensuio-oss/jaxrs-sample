package com.hotjoe.services.model;

import javax.persistence.*;

@Entity
@Table(name="orderdetails")
public class OrderDetails {
    @Id
    @Column(name = "orderNumber")
    @GeneratedValue()
    private Integer orderNumber;

    @ManyToOne()
    @JoinColumn(name = "productCode")
    private Product product;

    @Column(name = "quantityOrdered")
    private Integer quantityOrdered;
    @Column(name = "priceEach")
    private Double priceEach;
    @Column(name = "orderLineNumber")
    private Short orderLineNumber;

    public Integer getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(Integer orderNumber) {
        this.orderNumber = orderNumber;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product productCode) {
        this.product = productCode;
    }

    public Integer getQuantityOrdered() {
        return quantityOrdered;
    }

    public void setQuantityOrdered(Integer quantityOrdered) {
        this.quantityOrdered = quantityOrdered;
    }

    public Double getPriceEach() {
        return priceEach;
    }

    public void setPriceEach(Double priceEach) {
        this.priceEach = priceEach;
    }

    public Short getOrderLineNumber() {
        return orderLineNumber;
    }

    public void setOrderLineNumber(Short orderLineNumber) {
        this.orderLineNumber = orderLineNumber;
    }
}