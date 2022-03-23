package com.sdl.ecommerce.service.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.sdl.ecommerce.api.model.Breadcrumb;
import com.sdl.ecommerce.api.model.CategoryRef;
import com.sdl.ecommerce.api.model.Facet;

import java.util.ArrayList;
import java.util.List;

// TODO: Add Graph-QL support here!!!
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RestBreadcrumb implements Breadcrumb {

    private Breadcrumb breadcrumb;

    public RestBreadcrumb(Breadcrumb breadcrumb) {
        this.breadcrumb = breadcrumb;
    }

    @Override
    public String getTitle() {
        return this.breadcrumb.getTitle();
    }

    @Override
    public boolean isCategory() {
        return this.breadcrumb.isCategory();
    }

    @Override
    public CategoryRef getCategoryRef() {
        if ( this.breadcrumb.getCategoryRef() != null ) {
            return new RestCategoryRef(this.breadcrumb.getCategoryRef());
        }
        return null;
    }

    @Override
    public Facet getFacet() {
        if ( this.breadcrumb.getFacet() != null ) {
            return new RestFacet(this.breadcrumb.getFacet());
        }
        return null;
    }

    static public List<RestBreadcrumb> toRestList(List<Breadcrumb> breadcrumbs) {
        if ( breadcrumbs != null ) {
            ArrayList<RestBreadcrumb> restBreadcrumbs = new ArrayList<>();
            breadcrumbs.forEach(breadcrumb -> restBreadcrumbs.add(new RestBreadcrumb(breadcrumb)));
            return restBreadcrumbs;
        }
        return null;
    }
}
