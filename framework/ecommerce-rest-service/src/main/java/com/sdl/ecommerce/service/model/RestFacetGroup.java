package com.sdl.ecommerce.service.model;

import com.sdl.ecommerce.api.model.Facet;
import com.sdl.ecommerce.api.model.FacetGroup;
import com.sdl.ecommerce.api.model.NameValue;
import graphql.annotations.GraphQLDescription;
import graphql.annotations.GraphQLField;
import graphql.annotations.GraphQLName;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@GraphQLName("FacetGroup")
@GraphQLDescription("E-Commerce Facet Group")
public class RestFacetGroup implements FacetGroup {

    private FacetGroup facetGroup;

    @GraphQLField
    private List<RestFacet> facets = new ArrayList<>();

    public RestFacetGroup(FacetGroup facetGroup) {
        this.facetGroup = facetGroup;
        this.facetGroup.getFacets().forEach(facet -> facets.add(new RestFacet(facet)));
    }

    @GraphQLField
    @Override
    public String getId() {
        return this.facetGroup.getId();
    }

    @GraphQLField
    @Override
    public String getTitle() {
        return this.facetGroup.getTitle();
    }

    @GraphQLField
    @Override
    public String getType() {
        return this.facetGroup.getType();
    }

    @Override
    public List<Facet> getFacets() {
        return this.facets.stream().collect(Collectors.toList());
    }

    @GraphQLField
    @Override
    public boolean isCategory() {
        return this.facetGroup.isCategory();
    }

    @GraphQLField
    @Override
    public String getEditUrl() {
        return this.facetGroup.getEditUrl();
    }

    @Override
    public List<NameValue> getAttributes() {
        return this.facetGroup.getAttributes();
    }
}
