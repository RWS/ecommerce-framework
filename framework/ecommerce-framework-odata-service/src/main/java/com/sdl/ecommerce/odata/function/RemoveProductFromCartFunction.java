package com.sdl.ecommerce.odata.function;

import com.sdl.ecommerce.api.CartService;
import com.sdl.ecommerce.api.model.Cart;
import com.sdl.ecommerce.odata.datasource.ProductDataSource;
import com.sdl.ecommerce.odata.model.ODataCart;
import com.sdl.ecommerce.odata.service.ODataRequestContextHolder;
import com.sdl.odata.api.ODataException;
import com.sdl.odata.api.ODataNotImplementedException;
import com.sdl.odata.api.edm.annotations.EdmFunction;
import com.sdl.odata.api.edm.annotations.EdmParameter;
import com.sdl.odata.api.edm.annotations.EdmProperty;
import com.sdl.odata.api.edm.annotations.EdmReturnType;
import com.sdl.odata.api.edm.model.Operation;
import com.sdl.odata.api.processor.datasource.factory.DataSourceFactory;
import com.sdl.odata.api.service.ODataRequestContext;

/**
 * Remove Product From Cart Function
 *
 * @author nic
 */
@EdmFunction(
        name = "RemoveProductFromCartFunction",
        namespace = "SDL.ECommerce",
        isBound = false
)
@EdmReturnType(
        type = "Carts"
)
public class RemoveProductFromCartFunction implements Operation<ODataCart> {

    @EdmParameter(nullable = false)
    private String cartId;

    @EdmProperty(nullable = true)
    private String sessionId;

    @EdmParameter(nullable = false)
    private String productId;

    @Override
    public ODataCart doOperation(ODataRequestContext oDataRequestContext, DataSourceFactory dataSourceFactory) throws ODataException {
        ODataRequestContextHolder.set(oDataRequestContext);
        try {
            ProductDataSource productDataSource = (ProductDataSource) dataSourceFactory.getDataSource(oDataRequestContext, "SDL.ECommerce.Product");
            CartService cartService = productDataSource.getCartService();
            if (cartService != null) {
                Cart cart = cartService.removeProductFromCart(cartId, sessionId, productId);
                return new ODataCart(cart);
            }
            throw new ODataNotImplementedException("No cart service found!");
        }
        finally {
            ODataRequestContextHolder.clear();
        }
    }
}
