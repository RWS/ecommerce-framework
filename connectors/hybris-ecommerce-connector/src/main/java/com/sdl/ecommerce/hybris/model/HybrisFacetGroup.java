package com.sdl.ecommerce.hybris.model;

import com.sdl.ecommerce.api.model.Facet;
import com.sdl.ecommerce.api.model.FacetGroup;

import java.util.ArrayList;
import java.util.List;

/**
 * Hybris Facet Group
 *
 * @author nic
 */
public class HybrisFacetGroup implements FacetGroup {

    private String id;
    private String title;
    private boolean isCategory;
    private List<Facet> facets = new ArrayList<>();

    public HybrisFacetGroup(String id, String title, boolean isCategory) {
        this.id = id;
        this.title = title;
        this.isCategory = isCategory;
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
        return null;
    }
}
