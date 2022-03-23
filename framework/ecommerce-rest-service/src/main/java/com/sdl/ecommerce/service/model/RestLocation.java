package com.sdl.ecommerce.service.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.sdl.ecommerce.api.model.CategoryRef;
import com.sdl.ecommerce.api.model.FacetParameter;
import com.sdl.ecommerce.api.model.Location;
import com.sdl.ecommerce.api.model.ProductRef;
import graphql.annotations.GraphQLDescription;
import graphql.annotations.GraphQLField;
import graphql.annotations.GraphQLName;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * REST Location
 *
 * @author nic
 */
@GraphQLName("Location")
@GraphQLDescription("Location to a product, category or a static URL")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RestLocation implements Location {

    @GraphQLField
    private RestCategoryRef categoryRef;

    @GraphQLField
    private List<String> categoryFacets;

    @GraphQLField
    private RestProductRef productRef;

    @GraphQLField
    private String staticUrl;

    
    public RestLocation(Location location) {
        if ( location.getCategoryRef() != null ) {
            this.categoryRef = new RestCategoryRef(location.getCategoryRef());
        }
        this.categoryFacets = new ArrayList<>();
        if ( location.getFacets() != null ) {
            this.categoryFacets = location.getFacets().stream().map(facet -> facet.toUrl()).collect(Collectors.toList());
        }
        if ( location.getProductRef() != null ) {
            this.productRef = new RestProductRef(location.getProductRef());
        }
        this.staticUrl = location.getStaticUrl();
    }

    @Override
    public CategoryRef getCategoryRef() {
        if ( this.categoryRef != null ) {
            return this.categoryRef;
        }
        return null;
    }

    public List<String> getCategoryFacets() {
        return categoryFacets;
    }

    // TODO: Hide this one??????
    @Override
    public List<FacetParameter> getFacets() {
        if ( this.categoryFacets != null ) {
            return this.categoryFacets.stream().map(facet -> FacetParameter.fromUrl(facet)).collect(Collectors.toList());
        }
        return null;
    }

    public ProductRef getProductRef() {
        if ( this.productRef != null ) {
            return this.productRef;
        }
        return null;
    }

    public String getStaticUrl() {
        return this.staticUrl;
    }
}
