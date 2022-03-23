package com.sdl.ecommerce.api.model;

import java.util.List;

/**
 * Product Variant
 *
 * @author nic
 */
public interface ProductVariant {

    /**
     * @return variant product ID
     */
    String getId();

    /**
     * @return variant price
     */
    ProductPrice getPrice();

    /**
     * @return variant attributes
     */
    List<ProductAttribute> getAttributes();
}
