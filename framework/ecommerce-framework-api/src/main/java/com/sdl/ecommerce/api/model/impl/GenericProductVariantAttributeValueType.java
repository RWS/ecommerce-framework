package com.sdl.ecommerce.api.model.impl;

import com.sdl.ecommerce.api.model.ProductVariantAttributeValueType;

/**
 * Generic implementation of the ProductVariantAttributeValueType interface
 *
 * @author nic
 */
public class GenericProductVariantAttributeValueType implements ProductVariantAttributeValueType {

    private String id;
    private String value;
    private boolean isSelected;
    private boolean isApplicable;

    /**
     * Constructor
     * @param id
     * @param value
     * @param isSelected
     * @param isApplicable
     */
    public GenericProductVariantAttributeValueType(String id, String value, boolean isSelected, boolean isApplicable) {
        this.id = id;
        this.value = value;
        this.isSelected = isSelected;
        this.isApplicable = isApplicable;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public boolean isSelected() {
        return isSelected;
    }

    @Override
    public boolean isApplicable() {
        return isApplicable;
    }
}
