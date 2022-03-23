package com.sdl.ecommerce.service.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.sdl.ecommerce.api.model.ProductAttribute;
import com.sdl.ecommerce.api.model.ProductAttributeValue;
import com.sdl.ecommerce.api.model.ProductVariantAttributeValueType;
import com.sdl.ecommerce.service.ListHelper;
import graphql.annotations.GraphQLDescription;
import graphql.annotations.GraphQLField;
import graphql.annotations.GraphQLName;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * REST Product Attribute
 *
 * @author nic
 */
@GraphQLName("ProductAttribute")
@GraphQLDescription("Product Attribute")
@JsonInclude(JsonInclude.Include.NON_NULL)

public class RestProductAttribute implements ProductAttribute {

    @GraphQLField
    @GraphQLDescription("Id")
    private String id;

    @GraphQLField
    @GraphQLDescription("Name")
    private String name;

    @GraphQLField
    @GraphQLDescription("Indicates if the attribute is a single value or not")
    private boolean singleValue;

    @GraphQLField
    @GraphQLDescription("Product attribute value(s)")
    private List<RestProductAttributeValue> values;

    // TODO: Can we include the attributes in the type specification as well?

    public RestProductAttribute(ProductAttribute attribute) {
        this.id = attribute.getId();
        this.name = attribute.getName();
        this.values = ListHelper.createWrapperList(attribute.getValues(), ProductAttributeValue.class, RestProductAttributeValue.class);
        this.singleValue = attribute.isSingleValue();
    }

    @Override
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public boolean isSingleValue() {
        return singleValue;
    }

    public List<ProductAttributeValue> getValues() {
        return values.stream().collect(Collectors.toList());
    }
}
