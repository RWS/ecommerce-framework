package com.sdl.ecommerce.service.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.sdl.ecommerce.api.ECommerceException;
import com.sdl.ecommerce.api.model.Cart;
import com.sdl.ecommerce.api.model.CartItem;
import com.sdl.ecommerce.api.model.ProductPrice;
import com.sdl.ecommerce.service.ListHelper;
import graphql.annotations.GraphQLDescription;
import graphql.annotations.GraphQLField;
import graphql.annotations.GraphQLName;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * REST Cart
 *
 * @author nic
 */
@GraphQLName("Cart")
@GraphQLDescription("E-Commerce Cart")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RestCart implements Cart {

    private Cart cart;

    @GraphQLField
    private List<RestCartItem> cartItems;

    @GraphQLField
    private RestProductPrice totalPrice;

    public RestCart(Cart cart) {
        this.cart = cart;
        cartItems = ListHelper.createWrapperList(cart.getItems(), CartItem.class, RestCartItem.class);

        if ( cart.getTotalPrice() != null ) {
            this.totalPrice = new RestProductPrice(cart.getTotalPrice());
        }
    }

    @GraphQLField
    @Override
    public String getId() {
        return this.cart.getId();
    }

    @GraphQLField
    @Override
    public String getSessionId() {
        return this.cart.getSessionId();
    }

    @Override
    public List<CartItem> getItems() {
        return cartItems.stream().collect(Collectors.toList());
    }


    @Override
    public int count() {
        return this.cart.count();
    }

    @GraphQLField
    public int getCount() { return this.count(); }

    @Override
    public ProductPrice getTotalPrice() throws ECommerceException {
        return this.totalPrice;
    }

    @Override
    public Map<URI, Object> getDataToExposeToClaimStore() throws ECommerceException {
        // Not exposed to REST
        //
        return null;
    }
}
