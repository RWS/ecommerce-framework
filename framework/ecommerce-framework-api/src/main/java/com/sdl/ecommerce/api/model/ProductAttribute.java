package com.sdl.ecommerce.api.model;

import java.util.List;

/**
 * Product Variant Attribute
 *
 * @author nic
 */
public interface ProductAttribute {

    /**
     * @return id of the variant attribute
     */
    String getId();

    /**
     * @return name of the variant attribute (can be localized)
     */
    String getName(); // TODO: Should be called title instead???

    /**
     * @return if attribute is a single value or a list of values
     */
    boolean isSingleValue();

    /**
     * @return attribute values. If single value this is either empty or have one entry
     */
    List<ProductAttributeValue> getValues();

    // TODO: Expose type as well????
}
