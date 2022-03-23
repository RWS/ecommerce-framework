package com.sdl.ecommerce.odata.model;

import com.sdl.ecommerce.api.model.ProductVariantAttributeValueType;
import com.sdl.odata.api.edm.annotations.EdmComplex;
import com.sdl.odata.api.edm.annotations.EdmProperty;

/**
 * ODataProductVariantAttributeValueType
 *
 * @author nic
 */
@EdmComplex(name = "ProductVariantAttributeValueType", namespace = "SDL.ECommerce")
public class ODataProductVariantAttributeValueType implements ProductVariantAttributeValueType {

    @EdmProperty
    private String id;

    @EdmProperty
    private String value;

    @EdmProperty
    private boolean isSelected;

    @EdmProperty
    private boolean isApplicable;

    public ODataProductVariantAttributeValueType() {}
    public ODataProductVariantAttributeValueType(ProductVariantAttributeValueType valueType) {
        this.id = valueType.getId();
        this.value = valueType.getValue();
        this.isSelected = valueType.isSelected();
        this.isApplicable = valueType.isApplicable();
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public String getValue() {
        return this.value;
    }

    @Override
    public boolean isSelected() {
        return this.isSelected;
    }

    @Override
    public boolean isApplicable() {
        return this.isApplicable;
    }
}
