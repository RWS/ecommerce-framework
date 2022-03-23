package com.sdl.ecommerce.odata.model;

import com.sdl.ecommerce.api.model.ProductAttribute;
import com.sdl.ecommerce.api.model.ProductAttributeValue;
import com.sdl.odata.api.edm.annotations.EdmComplex;
import com.sdl.odata.api.edm.annotations.EdmProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * OData Product Attribute
 *
 * @author nic
 */
@EdmComplex(name="ProductAttribute", namespace = "SDL.ECommerce")
public class ODataProductAttribute implements ProductAttribute {

    @EdmProperty
    private String id;

    @EdmProperty
    private String name;

    @EdmProperty
    private boolean isSingleValue;

    @EdmProperty
    private List<ODataProductAttributeValue> values = new ArrayList<>();

    public ODataProductAttribute() {}
    public ODataProductAttribute(ProductAttribute productAttribute) {
        this.id = productAttribute.getId();
        this.name = productAttribute.getName();
        this.isSingleValue = productAttribute.isSingleValue();
        if ( productAttribute.getValues() != null ) {
            productAttribute.getValues().forEach(value -> this.values.add(new ODataProductAttributeValue(value)));
        }

    }

    @Override
    public String getId() {
        return null;
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public boolean isSingleValue() {
        return false;
    }

    @Override
    public List<ProductAttributeValue> getValues() {
        return this.values.stream().collect(Collectors.toList());
    }
}
