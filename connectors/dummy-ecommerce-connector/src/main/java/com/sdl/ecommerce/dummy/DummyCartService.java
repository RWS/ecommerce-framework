package com.sdl.ecommerce.dummy;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.sdl.ecommerce.api.CartService;
import com.sdl.ecommerce.api.ECommerceException;
import com.sdl.ecommerce.api.ProductDetailService;
import com.sdl.ecommerce.api.model.Cart;
import com.sdl.ecommerce.dummy.model.DummyCart;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * Dummy Cart Factory.
 * Creates a dummy cart which is handy in test & demo situations.
 *
 * @author nic
 */
@Component
public class DummyCartService implements CartService {

    @Autowired
    private ProductDetailService detailService;

    private Cache<String, DummyCart> cartStore = CacheBuilder.newBuilder()
            .maximumSize(100)
            .expireAfterWrite(30, TimeUnit.MINUTES).build();  // TODO: Have this cache time configurable

    @Override
    public Cart createCart() throws ECommerceException {
        DummyCart cart = new DummyCart(this.detailService);
        cartStore.put(cart.getId(), cart);
        return cart;
    }

    @Override
    public Cart addProductToCart(String cartId, String sessionId, String productId, int quantity) throws ECommerceException {
        DummyCart cart = this.cartStore.getIfPresent(cartId);
        if ( cart == null ) {
            cart = (DummyCart) this.createCart();
            cartStore.put(cart.getId(), cart);
        }
        if ( cart != null ) {
            cart.addProduct(productId, quantity);
            return cart;
        }
        return null;
    }

    @Override
    public Cart removeProductFromCart(String cartId, String sessionId, String productId) throws ECommerceException {
        DummyCart cart = this.cartStore.getIfPresent(cartId);
        if ( cart == null ) {
            cart = (DummyCart) this.createCart();
            cartStore.put(cart.getId(), cart);
        }
        if ( cart != null ) {
            cart.removeProduct(productId);
            return cart;
        }
        return null;
    }

    public Cart clearCart(String cartId) throws ECommerceException {
        DummyCart cart = this.cartStore.getIfPresent(cartId);
        if ( cart != null ) {
            cart.clear();
            return cart;
        }
        else {
            return createCart();
        }
    }
}
