package com.sdl.ecommerce.service.model;

import com.sdl.ecommerce.api.model.ProductAttributeValue;

public class RestProductAttributeValue implements ProductAttributeValue {

    private String value;
    private String presentationValue;

    public RestProductAttributeValue(ProductAttributeValue attributeValue) {
        this.value = attributeValue.getValue();
        this.presentationValue = attributeValue.getPresentationValue();
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
