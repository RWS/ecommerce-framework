package com.sdl.ecommerce.service.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.sdl.ecommerce.api.model.CategoryRef;
import graphql.annotations.GraphQLDescription;
import graphql.annotations.GraphQLField;
import graphql.annotations.GraphQLName;

/**
 * REST Category Ref
 *
 * @author nic
 */
@GraphQLName("CategoryRef")
@GraphQLDescription("Category reference")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RestCategoryRef extends CategoryRef {

    public RestCategoryRef(CategoryRef categoryRef) {
        super(categoryRef.getId(), categoryRef.getName(), categoryRef.getPath());
    }

    @GraphQLField
    @Override
    public String getId() {
        return super.getId();
    }

    @GraphQLField
    @Override
    public String getName() {
        return super.getName();
    }

    @GraphQLField
    @Override
    public String getPath() {
        return super.getPath();
    }
}
