package com.sdl.ecommerce.service.rest;

import com.sdl.ecommerce.api.CartService;
import com.sdl.ecommerce.api.model.Cart;
import com.sdl.ecommerce.service.model.ErrorMessage;
import com.sdl.ecommerce.service.model.RestCart;
import com.sdl.ecommerce.service.model.RestInputCartItem;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Cart Controller
 */
@RestController
@RequestMapping("/ecommerce.svc/rest/v1/cart")
@Slf4j
public class CartController {

    @Autowired(required = false)
    private CartService cartService;

    // TODO: Add error handling

    @RequestMapping(value="/", method = RequestMethod.POST)
    public ResponseEntity createCart()
    {
        if ( cartService != null ) {
            Cart cart = cartService.createCart();
            return new ResponseEntity<Cart>(new RestCart(cart), HttpStatus.OK);
        }
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(new ErrorMessage("Cart service is not available"));
    }

    @RequestMapping(value="/{cartId}", method = RequestMethod.PUT)
    public ResponseEntity updateCart(@PathVariable String cartId, @RequestBody ArrayList<RestInputCartItem> cartItems) {
        if ( cartService != null ) {
            if ( cartItems == null ) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorMessage("Missing cart items"));
            }

            // Validate the cart items
            //
            for ( RestInputCartItem cartItem : cartItems ) {
                if ( cartItem.getOperation() == null || !cartItem.getOperation().equals("add") && !cartItem.getOperation().equals("remove") ) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorMessage("Invalid cart item operation: " + cartItem.getOperation()));
                }
            }

            try {
                // Update cart
                //
                Cart cart = null;
                for (RestInputCartItem cartItem : cartItems) {
                    if (cartItem.getOperation().equals("add")) {
                        cart = this.cartService.addProductToCart(cartId, null, cartItem.getItemId(), cartItem.getQuantity());
                    } else if (cartItem.getOperation().equals("remove")) {
                        cart = this.cartService.removeProductFromCart(cartId, null, cartItem.getItemId());
                    }
                }
                return new ResponseEntity<Cart>(new RestCart(cart), HttpStatus.OK);
            }
            catch ( Exception e ) {
                log.error("Exception when updating cart: " + cartId, e);
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(new ErrorMessage(e.getMessage()));
            }
        }
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(new ErrorMessage("Cart service is not available"));
    }

}
