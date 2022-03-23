package com.sdl.ecommerce.service.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.sdl.ecommerce.api.model.CartItem;
import com.sdl.ecommerce.api.model.Product;
import com.sdl.ecommerce.api.model.ProductPrice;
import graphql.annotations.GraphQLDescription;
import graphql.annotations.GraphQLField;
import graphql.annotations.GraphQLName;

/**
 * REST Cart Item
 */
@GraphQLName("CartItem")
@GraphQLDescription("E-Commerce Cart Item")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RestCartItem implements CartItem {

    private CartItem cartItem;

    @GraphQLField
    private RestProductSummary product;

    @GraphQLField
    private RestProductPrice productPrice;

    public RestCartItem(CartItem cartItem) {
        this.cartItem = cartItem;
        if ( this.cartItem.getProduct() != null ) {
            this.product = new RestProductSummary(this.cartItem.getProduct());
        }
        if ( this.cartItem.getPrice() != null ) {
            this.productPrice = new RestProductPrice(this.cartItem.getPrice());
        }
    }

    @Override
    public ProductPrice getPrice() {
        return this.productPrice;
    }

    @Override
    public Product getProduct() {
        return this.product;
    }

    @GraphQLField
    @Override
    public int getQuantity() {
        return cartItem.getQuantity();
    }

}
