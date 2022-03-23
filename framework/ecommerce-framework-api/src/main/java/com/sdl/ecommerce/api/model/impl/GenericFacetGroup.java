package com.sdl.ecommerce.api.model.impl;

import com.sdl.ecommerce.api.model.Facet;
import com.sdl.ecommerce.api.model.FacetGroup;
import com.sdl.ecommerce.api.model.NameValue;

import java.util.ArrayList;
import java.util.List;

/**
 * Generic Facet Group
 *
 * @author nic
 */
public class GenericFacetGroup implements FacetGroup {

    private String id;
    private String title;
    private boolean isCategory;
    private List<Facet> facets = new ArrayList<>();
    private List<NameValue> attributes = null;
    private String editUrl;

    public GenericFacetGroup(String id, String title, boolean isCategory) {
        this.id = id;
        this.title = title;
        this.isCategory = isCategory;
    }

    public GenericFacetGroup(String id, String title, boolean isCategory, String editUrl) {
        this.id = id;
        this.title = title;
        this.isCategory = isCategory;
        this.editUrl = editUrl;
    }

    public GenericFacetGroup(String id, String title, boolean isCategory, String editUrl, List<NameValue> attributes) {
        this.id = id;
        this.title = title;
        this.isCategory = isCategory;
        this.editUrl = editUrl;
        this.attributes = attributes;
    }

    @Override
    public List<Facet> getFacets() {
        return this.facets;
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public String getTitle() {
        return this.title;
    }

    @Override
    public String getType() {
        if ( this.isCategory() ) {
            return "cat"; // TODO: Use standardized enum type here
        }
        else {
            return "facet";
        }
    }

    @Override
    public boolean isCategory() {
        return this.isCategory;
    }

    @Override
    public String getEditUrl() {
        return this.editUrl;
    }

    @Override
    public List<NameValue> getAttributes() {
        return attributes;
    }
}
