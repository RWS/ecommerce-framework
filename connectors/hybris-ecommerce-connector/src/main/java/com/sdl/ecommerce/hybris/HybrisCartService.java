package com.sdl.ecommerce.hybris;

import com.sdl.ecommerce.api.CartService;
import com.sdl.ecommerce.api.ECommerceException;
import com.sdl.ecommerce.api.ProductDetailService;
import com.sdl.ecommerce.api.model.Cart;
import com.sdl.ecommerce.hybris.api.HybrisClientManager;
import com.sdl.ecommerce.hybris.model.HybrisCart;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

/**
 * Hybris Cart Factory
 *
 * @author nic
 */
@Component
public class HybrisCartService implements CartService {

    @Autowired
    private HybrisClientManager hybrisClientManager;

    @Autowired
    private ProductDetailService productDetailService;

    @Override
    public Cart createCart() throws ECommerceException {
        String sessionId = hybrisClientManager.getInstance().createCart();
        com.sdl.ecommerce.hybris.api.model.Cart cart = hybrisClientManager.getInstance().getCart(sessionId);
        return new HybrisCart(cart, toExternalSessionId(sessionId), this.productDetailService);
    }

    @Override
    public Cart addProductToCart(String cartId, String sessionId, String productId, int quantity) throws ECommerceException {

        // TODO: Clear up when it comes to cart ID vs session ID
        // For now we use cart ID as session ID
        //
        sessionId = cartId;
        try {
            com.sdl.ecommerce.hybris.api.model.Cart cart = this.hybrisClientManager.getInstance().addItemToCart(toHybrisSessionId(sessionId), productId, quantity);
            return new HybrisCart(cart, sessionId, this.productDetailService);
        }
        catch ( Exception e ) {
            throw new ECommerceException("Could not add product to cart.", e);
        }
    }

    @Override
    public Cart removeProductFromCart(String cartId, String sessionId, String productId) throws ECommerceException {

        // TODO: Clear up when it comes to cart ID vs session ID
        // For now we use cart ID as session ID
        //
        sessionId = cartId;
        try {
            com.sdl.ecommerce.hybris.api.model.Cart cart = this.hybrisClientManager.getInstance().removeItemFromCart(toHybrisSessionId(sessionId), productId);
            return new HybrisCart(cart, sessionId, this.productDetailService);
        }
        catch ( Exception e ) {
            throw new ECommerceException("Could not remove product to cart.", e);
        }
    }

    private String toHybrisSessionId(String sessionId) {
        return "JSESSIONID=" + sessionId + "; Path=/rest";
    }

    private String toExternalSessionId(String sessionId) {
        return sessionId.replace("JSESSIONID=", "").replace("; Path=/rest", "");
    }

}
