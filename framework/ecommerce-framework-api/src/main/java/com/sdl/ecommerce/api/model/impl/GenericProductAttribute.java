package com.sdl.ecommerce.api.model.impl;

import com.sdl.ecommerce.api.model.ProductAttribute;
import com.sdl.ecommerce.api.model.ProductAttributeValue;

import java.util.ArrayList;
import java.util.List;

/**
 * Generic implementation of the {@link ProductAttribute} interface
 *
 * @author nic
 */
public class GenericProductAttribute implements ProductAttribute {

    private String id;
    private String name;
    private List<ProductAttributeValue> values = new ArrayList<>();
    private boolean isSingleValue;

    /**
     * Constructor
     * @param id
     * @param name
     * @param value
     */
    public GenericProductAttribute(String id, String name, ProductAttributeValue value) {
        this.id = id;
        this.name = name;
        this.values.add(value);
        this.isSingleValue = true;
    }

    /**
     * Constructor
     * @param id
     * @param name
     * @param values
     */
    public GenericProductAttribute(String id, String name, List<ProductAttributeValue> values) {
        this.id = id;
        this.name = name;
        this.values.addAll(values);
        this.isSingleValue = false;
    }

    /**
     * Constructor
     * @param id
     * @param name
     * @param values
     * @param isSingleValue
     */
    public GenericProductAttribute(String id, String name, List<ProductAttributeValue> values, boolean isSingleValue) {
        this.id = id;
        this.name = name;
        this.values.addAll(values);
        this.isSingleValue = isSingleValue;
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public List<ProductAttributeValue> getValues() {
        return values;
    }

    @Override
    public boolean isSingleValue() {
        return this.isSingleValue;
    }

}
