package com.sdl.ecommerce.api.model.impl;

import com.sdl.ecommerce.api.model.ProductAttribute;
import com.sdl.ecommerce.api.model.ProductPrice;
import com.sdl.ecommerce.api.model.ProductVariant;

import java.util.List;

/**
 * Generic Product Variant
 *
 * @author nic
 */
public class GenericProductVariant implements ProductVariant {

    private String id;
    private ProductPrice price;
    private List<ProductAttribute> attributes;

    public GenericProductVariant(String id, ProductPrice price, List<ProductAttribute> attributes) {
        this.id = id;
        this.price = price;
        this.attributes = attributes;
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public ProductPrice getPrice() {
        return this.price;
    }

    @Override
    public List<ProductAttribute> getAttributes() {
        return this.attributes;
    }

    public ProductAttribute getAttribute(String id) {
        if ( this.attributes != null ) {
            for ( ProductAttribute attribute : this.attributes ) {
                if ( attribute.getId().equals(id) ) {
                    return attribute;
                }
            }
        }
        return null;
    }
}
