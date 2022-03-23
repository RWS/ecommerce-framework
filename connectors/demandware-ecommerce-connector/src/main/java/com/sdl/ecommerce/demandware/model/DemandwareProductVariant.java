package com.sdl.ecommerce.demandware.model;

import com.sdl.ecommerce.api.model.ProductAttribute;
import com.sdl.ecommerce.api.model.ProductPrice;
import com.sdl.ecommerce.api.model.ProductVariant;
import com.sdl.ecommerce.api.model.impl.GenericProductAttribute;
import com.sdl.ecommerce.api.model.impl.GenericProductAttributeValue;
import com.sdl.ecommerce.demandware.api.model.VariationAttribute;
import com.sdl.ecommerce.demandware.api.model.VariationAttributeValue;

import java.util.ArrayList;
import java.util.List;

/**
 * DemandwareProductVariant
 *
 * @author nic
 */
public class DemandwareProductVariant implements ProductVariant {

    private String id;
    private ProductPrice price;
    private List<ProductAttribute> attributes = new ArrayList<>();


    public DemandwareProductVariant(com.sdl.ecommerce.demandware.api.model.ProductVariant productVariant, List<VariationAttribute> variationAttributes, String currency) {
        this.id = productVariant.getProduct_id();
        for ( String attributeId : productVariant.getVariation_values().keySet() ) {
            String valueId = productVariant.getVariation_values().get(attributeId);
            String attributeName = null;
            String value = null;
            for ( VariationAttribute variationAttribute : variationAttributes ) {
                if ( variationAttribute.getId().equals(attributeId) ) {
                    attributeName = variationAttribute.getName();
                    for ( VariationAttributeValue variationAttributeValue : variationAttribute.getValues() ) {
                        if ( variationAttributeValue.getValue().equals(valueId) ) {
                            value = variationAttributeValue.getName();
                        }
                    }
                }
            }
            attributes.add(new GenericProductAttribute(attributeId, attributeName, new GenericProductAttributeValue(valueId, value)));
        }
        this.price = new DemandwarePrice(productVariant.getPrice(), currency);
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
}
