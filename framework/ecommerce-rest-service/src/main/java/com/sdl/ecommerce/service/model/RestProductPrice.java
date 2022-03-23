package com.sdl.ecommerce.service.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.sdl.ecommerce.api.model.ProductPrice;
import graphql.annotations.GraphQLDescription;
import graphql.annotations.GraphQLField;
import graphql.annotations.GraphQLName;

/**
 * REST Product Price
 *
 * @author nic
 */
@GraphQLName("ProductPrice")
@GraphQLDescription("Product Price")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RestProductPrice implements ProductPrice {

    @GraphQLField
    private float price;

    @GraphQLField
    private String formattedPrice;

    @GraphQLField
    private String currency;

    public RestProductPrice() {}

    public RestProductPrice(ProductPrice productPrice) {
        this.price = productPrice.getPrice();
        this.formattedPrice = productPrice.getFormattedPrice(); // TODO: Should this be only presentation related???
        this.currency = productPrice.getCurrency();
    }

    @Override
    public float getPrice() {
        return this.price;
    }

    @Override
    public String getFormattedPrice() {
        return this.formattedPrice;
    }

    @Override
    public String getCurrency() {
        return this.currency;
    }
}
