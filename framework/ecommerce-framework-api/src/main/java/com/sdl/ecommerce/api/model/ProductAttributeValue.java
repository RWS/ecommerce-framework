package com.sdl.ecommerce.api.model;

/**
 * Interface for product attribute values.
 */
public interface ProductAttributeValue {

    /**
     * Attribute value. Is system value of a specific color, size etc.
     * @return
     */
    String getValue();

    /**
     * Attribute presentation value. This value can be localized.
     * @return
     */
    String getPresentationValue();
}
