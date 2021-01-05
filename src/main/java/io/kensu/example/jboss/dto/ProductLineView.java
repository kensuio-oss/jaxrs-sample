package io.kensu.example.jboss.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.kensu.example.jboss.model.ProductLine;

import java.util.Collection;
import java.util.stream.Collectors;

public class ProductLineView {
    @JsonProperty
    public final String productLine;
    @JsonProperty
    public final String textDescription;
    @JsonProperty
    public final String htmlDescription;
    @JsonProperty
    public final Collection<ProductView> products;

    public ProductLineView(ProductLine productLine) {
        this.productLine = productLine.getProductLine();
        this.textDescription = productLine.getTextDescription();
        this.htmlDescription = productLine.getHtmlDescription();
        this.products = productLine.getProducts().stream().map(ProductView::new).collect(Collectors.toList());
    }
}
