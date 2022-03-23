package com.sdl.ecommerce.service.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.sdl.ecommerce.api.model.ProductVariantAttributeType;
import com.sdl.ecommerce.api.model.ProductVariantAttributeValueType;
import com.sdl.ecommerce.service.ListHelper;
import graphql.annotations.GraphQLDescription;
import graphql.annotations.GraphQLField;
import graphql.annotations.GraphQLName;

import java.util.List;
import java.util.stream.Collectors;

@GraphQLDescription("E-Commerce Product Variant Attribute Type")
@GraphQLName("ProductVariantAttributeType")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RestProductVariantAttributeType implements ProductVariantAttributeType {

    private ProductVariantAttributeType variantAttributeType;

    @GraphQLField
    private List<RestProductVariantAttributeValueType> values;

    public RestProductVariantAttributeType(ProductVariantAttributeType variantAttributeType) {
        this.variantAttributeType = variantAttributeType;
        this.values = ListHelper.createWrapperList(variantAttributeType.getValues(), ProductVariantAttributeValueType.class, RestProductVariantAttributeValueType.class);
    }

    @GraphQLField
    @Override
    public String getId() {
        return this.variantAttributeType.getId();
    }

    @GraphQLField
    @Override
    public String getName() {
        return this.variantAttributeType.getName();
    }

    @Override
    public List<ProductVariantAttributeValueType> getValues() {
        return this.values.stream().collect(Collectors.toList());
    }
}
