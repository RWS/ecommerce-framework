package com.sdl.ecommerce.service.model;

import com.sdl.ecommerce.api.model.Facet;
import graphql.annotations.GraphQLDescription;
import graphql.annotations.GraphQLField;
import graphql.annotations.GraphQLName;

import java.util.List;

@GraphQLName("Facet")
@GraphQLDescription("E-Commerce Facet")
public class RestFacet implements Facet {

    private Facet facet;

    public RestFacet(Facet facet) {
        this.facet = facet;
    }

    @GraphQLField
    @Override
    public String getId() {
        return this.facet.getId();
    }

    @GraphQLField
    @Override
    public String getTitle() {
        return this.facet.getTitle();
    }

    @GraphQLField
    @Override
    public int getCount() {
        return this.facet.getCount();
    }

    @GraphQLField
    @Override
    public boolean isSelected() {
        return this.facet.isSelected();
    }

    @GraphQLField
    @Override
    public boolean isCategory() {
        return this.facet.isCategory();
    }

    @GraphQLField
    @Override
    public FacetType getType() {
        return this.facet.getType();
    }

    @GraphQLField
    @Override
    public String getValue() {
        return this.facet.getValue();
    }

    @GraphQLField
    @Override
    public List<String> getValues() {
        return this.facet.getValues();
    }
}
