package com.sdl.ecommerce.fredhopper;

import com.fredhopper.lang.query.Query;
import com.fredhopper.webservice.client.Universe;
import com.sdl.ecommerce.fredhopper.model.FredhopperProduct;

import java.util.Map;

/**
 * Product Variant Builder
 *
 * @author nic
 */
public interface ProductVariantBuilder {

    /**
     * Contribute to query.
     * @param query
     * @param productId
     * @param variantAttributes
     */
    void contributeToQuery(Query query, String productId, Map<String,String> variantAttributes);

    /**
     * Build variants data on provided product.
     * @param product
     * @param universe
     */
    void buildVariants(FredhopperProduct product, Universe universe);

}
