package com.sdl.ecommerce.service.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.sdl.ecommerce.api.model.ProductRef;
import graphql.annotations.GraphQLDescription;
import graphql.annotations.GraphQLField;
import graphql.annotations.GraphQLName;

/**
 * REST Product Ref
 *
 * @author nic
 */
@GraphQLName("ProductRef")
@GraphQLDescription("Product reference")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RestProductRef extends ProductRef {

    public RestProductRef(ProductRef productRef) {
        super(productRef.getId(), productRef.getName());
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
}
