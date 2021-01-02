package com.hotjoe.services.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.hotjoe.services.model.OrderDetails;

public class OrderDetailsView {
    @JsonProperty
    public final Integer orderNumber;

    @JsonProperty
    public final ProductView product;

    @JsonProperty
    public final Integer quantityOrdered;
    @JsonProperty
    public final Double priceEach;
    @JsonProperty
    public final Short orderLineNumber;

    public OrderDetailsView(OrderDetails orderDetails) {
        this.orderNumber = orderDetails.getOrderNumber();
        this.product = new ProductView(orderDetails.getProduct());
        this.quantityOrdered = orderDetails.getQuantityOrdered();
        this.priceEach = orderDetails.getPriceEach();
        this.orderLineNumber = orderDetails.getOrderLineNumber();
    }
}
