package com.hotjoe.services.model;

import java.sql.Blob;
import java.util.Collection;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.GeneratedValue;
import javax.persistence.OneToMany;
 
@Entity
@Table(name="productlines")
public class ProductLine {
    @Id
    @Column(name = "productLine")
    @GeneratedValue()
    private String productLine;

    @Column(name = "textDescription")
    private String textDescription;
    @Column(name = "htmlDescription")
    private String htmlDescription;
    @Column(name = "image")
    //@Lob
    private /*Blob*/ String image;
    
    @OneToMany(mappedBy="productLine", fetch = FetchType.EAGER)
    private Collection<Product> products;

    public String getTextDescription() {
        return textDescription;
    }

    public void setTextDescription(String textDescription) {
        this.textDescription = textDescription;
    }

    public String getHtmlDescription() {
        return htmlDescription;
    }

    public void setHtmlDescription(String htmlDescription) {
        this.htmlDescription = htmlDescription;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public Collection<Product> getProducts() {
        return products;
    }

    public void setProducts(Collection<Product> products) {
        this.products = products;
    }

    public String getProductLine() {
        return productLine;
    }
}