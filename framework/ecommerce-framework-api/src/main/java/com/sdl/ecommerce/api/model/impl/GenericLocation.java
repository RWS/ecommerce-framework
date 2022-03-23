package com.sdl.ecommerce.api.model.impl;

import com.sdl.ecommerce.api.model.*;

import java.util.List;

/**
 * Generic implementation of the Location interface
 *
 * @author nic
 */
public class GenericLocation implements Location {

    private CategoryRef categoryRef;

    private List<FacetParameter> facets;

    private ProductRef productRef;

    private String staticUrl; // Static URL managed from the E-Commerce system

    public GenericLocation(Category category) {
        this.categoryRef = new CategoryRef(category);
    }

    public GenericLocation(Category category, List<FacetParameter> facets) {
        this.categoryRef = category != null ? new CategoryRef(category) : null;
        this.facets = facets;
    }

    public GenericLocation(Product product) {
        this.productRef = new ProductRef(product);
    }

    public GenericLocation(String staticUrl) {
        this.staticUrl = staticUrl;
    }

    @Override
    public CategoryRef getCategoryRef() {
        return this.categoryRef;
    }

    @Override
    public List<FacetParameter> getFacets() {
        return facets;
    }

    @Override
    public ProductRef getProductRef() {
        return this.productRef;
    }

    @Override
    public String getStaticUrl() {
        return staticUrl;
    }
}
