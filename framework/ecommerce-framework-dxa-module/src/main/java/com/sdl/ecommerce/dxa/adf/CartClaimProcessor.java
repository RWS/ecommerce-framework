package com.sdl.ecommerce.dxa.adf;

import com.sdl.ecommerce.api.ECommerceException;
import com.sdl.ecommerce.api.model.Cart;
import com.tridion.ambientdata.AmbientDataException;
import com.tridion.ambientdata.claimstore.ClaimStore;
import com.tridion.ambientdata.processing.ClaimProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.Map;

/**
 * CartClaimProcessor
 *
 * @author nic
 */
public class CartClaimProcessor implements ClaimProcessor {

    private static final Logger LOG = LoggerFactory.getLogger(CartClaimProcessor.class);

    static final URI SESSION_ATTRIBUTES_URI = URI.create("taf:session:attributes");


    @Override
    public void onRequestStart(ClaimStore claimStore) throws AmbientDataException {

        Cart cart = (Cart) claimStore.get(Cart.CART_URI);

        if ( cart == null ) {
            Map<String,Object> sessionAttributes = (Map<String,Object>) claimStore.get(SESSION_ATTRIBUTES_URI);
            cart = (Cart) sessionAttributes.get(Cart.CART_URI.toString());
            if ( cart != null ) {
                claimStore.put(Cart.CART_URI, cart);
            }
        }

        if ( cart != null ) {
            try {
                // TODO: Have some kind of cart.isChanged function here or E-TAG like identifier
                //
                Map<URI, Object> cartClaimValues = cart.getDataToExposeToClaimStore();
                for (URI uri : cartClaimValues.keySet()) {
                    claimStore.put(uri, cartClaimValues.get(uri));
                }
            } catch (ECommerceException e) {
                LOG.error("Could not get data from the E-Commerce Shopping Cart.", e);
            }
        }

    }


    @Override
    public void onRequestEnd(ClaimStore claimStore) throws AmbientDataException {

    }

    @Override
    public void onSessionStart(ClaimStore claimStore) throws AmbientDataException {

    }
}
