package com.sdl.ecommerce.service.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.sdl.ecommerce.api.model.ProductVariantAttributeValueType;
import graphql.annotations.GraphQLDescription;
import graphql.annotations.GraphQLField;
import graphql.annotations.GraphQLName;

@GraphQLDescription("E-Commerce Product Variant Attribute Value Type")
@GraphQLName("ProductVariantAttributeValueType")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RestProductVariantAttributeValueType implements ProductVariantAttributeValueType {

    private ProductVariantAttributeValueType valueType;

    public RestProductVariantAttributeValueType(ProductVariantAttributeValueType valueType) {
        this.valueType = valueType;
    }

    @GraphQLField
    @Override
    public String getId() {
        return this.valueType.getId();
    }

    @GraphQLField
    @Override
    public String getValue() {
        return this.valueType.getValue();
    }

    @GraphQLField
    @Override
    public boolean isSelected() {
        return this.valueType.isSelected();
    }

    @GraphQLField
    @Override
    public boolean isApplicable() {
        return this.valueType.isApplicable();
    }
}
