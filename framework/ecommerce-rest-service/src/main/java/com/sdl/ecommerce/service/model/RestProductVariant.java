package com.sdl.ecommerce.service.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.sdl.ecommerce.api.model.ProductPrice;
import com.sdl.ecommerce.api.model.ProductVariant;
import com.sdl.ecommerce.api.model.ProductAttribute;
import graphql.annotations.GraphQLDescription;
import graphql.annotations.GraphQLField;
import graphql.annotations.GraphQLName;

import java.util.List;

@GraphQLDescription("E-Commerce Product Variant")
@GraphQLName("ProductVariant")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RestProductVariant implements ProductVariant {

    private ProductVariant productVariant;

    @GraphQLField
    private RestProductPrice productPrice;

    public RestProductVariant(ProductVariant productVariant) {
        this.productVariant = productVariant;
        if ( this.productVariant.getPrice() != null ) {
            this.productPrice = new RestProductPrice(this.productVariant.getPrice());
        }
    }

    @GraphQLField
    @Override
    public String getId() {
        return this.productVariant.getId();
    }

    @Override
    public ProductPrice getPrice() {
        return this.productPrice;
    }

    @Override
    public List<ProductAttribute> getAttributes() {
        return this.productVariant.getAttributes();
    }
}
