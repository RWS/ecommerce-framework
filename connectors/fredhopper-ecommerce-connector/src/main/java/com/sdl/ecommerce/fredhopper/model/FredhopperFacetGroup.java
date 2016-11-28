package com.sdl.ecommerce.fredhopper.model;

import com.fredhopper.webservice.client.Filter;
import com.sdl.ecommerce.api.model.Editable;
import com.sdl.ecommerce.api.model.Facet;
import com.sdl.ecommerce.api.model.FacetGroup;

import java.util.ArrayList;
import java.util.List;

/**
 * FredhopperFacetGroup
 *
 * @author nic
 */
public class FredhopperFacetGroup implements FacetGroup {

    private String id;
    private String title;
    private String type = "none";
    private List<Facet> facets = new ArrayList<>();
    private String editUrl;

    public FredhopperFacetGroup(Filter filter, String editUrl) {
        this.id = filter.getFacetid();
        this.title = filter.getTitle();
        this.editUrl = editUrl;
        if ( filter.getBasetype() != null ) {
            this.type = filter.getBasetype().value();
        }
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public List<Facet> getFacets() {
        return facets;
    }

    @Override
    public boolean isCategory() {
        return this.type.equals("cat");
    }

    @Override
    public String getEditUrl() {
        return this.editUrl;
    }
}
