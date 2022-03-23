package com.sdl.ecommerce.odata.model;

import com.sdl.ecommerce.api.model.ProductAttributeValue;
import com.sdl.odata.api.edm.annotations.EdmComplex;
import com.sdl.odata.api.edm.annotations.EdmProperty;

import java.util.ArrayList;
import java.util.List;

/**
 * OData Product Attribute Value
 */
@EdmComplex(name="ProductAttributeValue", namespace = "SDL.ECommerce")
public class ODataProductAttributeValue implements ProductAttributeValue {

    @EdmProperty
    private String value;

    @EdmProperty
    private String presentationValue;

    public ODataProductAttributeValue() {}
    public ODataProductAttributeValue(ProductAttributeValue value) {
        this.value = value.getValue();
        this.presentationValue = value.getPresentationValue();
    }

    @Override
    public String getValue() {
        return this.value;
    }

    @Override
    public String getPresentationValue() {
        return this.presentationValue;
    }
}
