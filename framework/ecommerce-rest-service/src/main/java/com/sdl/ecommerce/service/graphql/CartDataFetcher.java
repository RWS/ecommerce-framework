package com.sdl.ecommerce.service.graphql;

import com.sdl.ecommerce.api.CartService;
import com.sdl.ecommerce.service.model.RestCart;
import graphql.schema.DataFetchingEnvironment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * CartDataFetcher
 *
 * @author nic
 */
@Component
public class CartDataFetcher {

    @Autowired(required = false)
    private CartService cartService;

    public RestCart addToCart(DataFetchingEnvironment environment) {

        // TODO: Do something...

        return null;
    }

    public RestCart removeFromCart(DataFetchingEnvironment environment) {

        // TODO: Do somethin....

        return null;
    }
    
}
