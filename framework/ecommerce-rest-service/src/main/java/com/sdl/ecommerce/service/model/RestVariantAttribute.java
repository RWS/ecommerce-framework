package com.sdl.ecommerce.service.model;

import graphql.annotations.GraphQLDescription;
import graphql.annotations.GraphQLField;
import graphql.annotations.GraphQLName;
import lombok.Getter;
import lombok.Setter;

/**
 * RestVariantAttribute
 *
 * @author nic
 */
@Getter
@Setter
@GraphQLName("VariantAttribute")
@GraphQLDescription("Variant Attribute")
public class RestVariantAttribute {

    @GraphQLField
    @GraphQLDescription("Variant Attribute Name")
    private String name;

    @GraphQLField
    @GraphQLDescription("Variant Attribute Value")
    private String value;
}
