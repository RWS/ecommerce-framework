package com.sdl.ecommerce.odata.model;

import com.sdl.ecommerce.api.model.ProductPrice;
import com.sdl.ecommerce.api.model.ProductVariant;
import com.sdl.ecommerce.api.model.ProductAttribute;
import com.sdl.odata.api.edm.annotations.EdmComplex;
import com.sdl.odata.api.edm.annotations.EdmProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * ODataProductVariant
 *
 * @author nic
 */
@EdmComplex(name = "ProductVariant", namespace = "SDL.ECommerce")
public class ODataProductVariant implements ProductVariant {

    @EdmProperty
    private String id;

    @EdmProperty
    private ODataProductPrice price;

    @EdmProperty
    private List<ODataProductAttribute> attributes;

    public ODataProductVariant() {}
    public ODataProductVariant(ProductVariant productVariant) {
        this.id = productVariant.getId();
        if ( productVariant.getPrice() != null ) {
            this.price = new ODataProductPrice(productVariant.getPrice());
        }
        if ( productVariant.getAttributes() != null ) {
            this.attributes = new ArrayList<>();
            productVariant.getAttributes().forEach(attribute -> this.attributes.add(new ODataProductAttribute(attribute)));
        }
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
        return attributes.stream().collect(Collectors.toList());
    }
}
