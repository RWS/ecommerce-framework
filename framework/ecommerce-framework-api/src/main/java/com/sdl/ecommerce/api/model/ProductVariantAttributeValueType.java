package com.sdl.ecommerce.api.model;

/**
 * Product Variant Attribute Value Type
 *
 * @author nic
 */
public interface ProductVariantAttributeValueType {

    /**
     * @return value id
     */
    String getId();

    /**
     * @return value (can be localized)
     */
    String getValue();

    /**
     * @return if selected
     */
    boolean isSelected();

    /**
     * @return if applicable for current selection (e.g. a size which is not available for selected color)
     */
    boolean isApplicable();

}
