package com.sdl.ecommerce.hybris.model;

import com.sdl.ecommerce.api.ECommerceException;
import com.sdl.ecommerce.api.ProductDetailResult;
import com.sdl.ecommerce.api.ProductDetailService;
import com.sdl.ecommerce.api.model.Cart;
import com.sdl.ecommerce.api.model.CartItem;
import com.sdl.ecommerce.api.model.ProductPrice;
import com.sdl.ecommerce.api.model.impl.GenericCartItem;
import com.sdl.ecommerce.hybris.api.HybrisClient;
import com.sdl.ecommerce.hybris.api.model.Entry;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Hybris Cart
 *
 * @author nic
 */
public class HybrisCart implements Cart {


    private List<CartItem> items = new ArrayList<>();
    private String sessionId;
    private int count = 0;
    private ProductPrice totalPrice = null;
    private float shipping = 0f;
    private float tax = 0f;
    private com.sdl.ecommerce.hybris.api.model.Cart cart = null;

    public HybrisCart(com.sdl.ecommerce.hybris.api.model.Cart cart, String sessionId, ProductDetailService detailService) {
        this.cart = cart;
        this.sessionId = sessionId;
        this.totalPrice = new HybrisPrice(cart.getTotalPrice());
        this.shipping = 0f; // TODO: How to calculate shipping costs
        if ( cart.getTotalTax() != null ) {
            this.tax = Float.parseFloat(cart.getTotalTax().getValue());
        }
        else {
            this.tax = 0f;
        }
        this.count = cart.getTotalUnitCount();
        if ( cart.getEntries() != null ) {
            for ( Entry entry : cart.getEntries() ) {
                ProductDetailResult productDetailResult = detailService.getDetail(entry.getProduct().getCode());
                CartItem item = new GenericCartItem(productDetailResult.getProductDetail(), entry.getQuantity(), new HybrisPrice(entry.getBasePrice()));
                this.items.add(item);
            }
        }
    }

    @Override
    public String getId() throws ECommerceException {
        return this.sessionId;
        //return this.cart.getCode();
    }

    // TODO: Remove session ID
    @Override
    public String getSessionId() {
        return this.sessionId;
    }

    @Override
    public List<CartItem> getItems() throws ECommerceException {
        return this.items;
    }

    @Override
    public int count() throws ECommerceException {
        return this.count;
    }

    @Override
    public ProductPrice getTotalPrice() throws ECommerceException {
        return this.totalPrice;
    }

    // TODO: SHOULD WE EXPOSE THESE KIND OF VALUES AS WELL????
    public float getShipping() {
        return shipping;
    }

    public float getTax() {
        return tax;
    }


    @Override
    public Map<URI,Object> getDataToExposeToClaimStore() throws ECommerceException {

        Map<URI,Object> claims = new HashMap<>();
        claims.put(Cart.CART_ITEMS_URI, this.count());
        claims.put(Cart.CART_TOTAL_PRICE_URI, this.getTotalPrice().getPrice());
        return claims;
    }

    @Override
    public String toString() {
        return "HybrisCart {" +
                "id=" + cart.getCode() +
                ", items=" + count +
                ", totalPrice=" + totalPrice +
                ", shipping=" + shipping +
                ", tax=" + tax +
                '}';
    }
}
