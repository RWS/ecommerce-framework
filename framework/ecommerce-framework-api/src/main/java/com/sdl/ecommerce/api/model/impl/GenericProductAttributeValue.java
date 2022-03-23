package com.sdl.ecommerce.api.model.impl;

import com.sdl.ecommerce.api.model.ProductAttributeValue;

import java.util.Objects;

/**
 * Generic implementation of the {@link ProductAttributeValue} interface
 */
public class GenericProductAttributeValue implements ProductAttributeValue {

    private String value;
    private String presentationValue;

    public GenericProductAttributeValue(String value) {
        this.value = value;
        this.presentationValue = value;
    }

    public GenericProductAttributeValue(String value, String presentationValue) {
        this.value = value;
        this.presentationValue = presentationValue;
        if ( this.value == null && this.presentationValue != null ) {
            this.value = this.presentationValue;
        }
    }

    @Override
    public String getValue() {
        return this.value;
    }

    @Override
    public String getPresentationValue() {
        return this.presentationValue;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GenericProductAttributeValue that = (GenericProductAttributeValue) o;
        return Objects.equals(value, that.value) &&
                Objects.equals(presentationValue, that.presentationValue);
    }

    @Override
    public int hashCode() {

        return Objects.hash(value, presentationValue);
    }
}
