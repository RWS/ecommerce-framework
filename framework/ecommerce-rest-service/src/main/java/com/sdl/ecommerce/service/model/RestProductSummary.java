package com.sdl.ecommerce.service.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sdl.ecommerce.api.model.*;
import com.sdl.ecommerce.service.ListHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * REST Product Summary
 * Gives a product summary. Is only used for the REST API. In Graph-QL you can specify what attributes you're interested in
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "id", "masterId", "variantId", "name", "price", "thumbnailUrl", "attributes"})
public class RestProductSummary implements Product {

    private Product product;

    private List<RestProductAttribute> attributes;


    public RestProductSummary(Product product) {
        this.product = product;
        this.attributes = ListHelper.createWrapperList(product.getAttributes(), ProductAttribute.class, RestProductAttribute.class);
    }

    @Override
    public String getId() {
        return this.product.getId();
    }

    @Override
    public String getMasterId() {
        return this.product.getMasterId();
    }

    @Override
    public String getVariantId() {
        return this.product.getVariantId();
    }

    @Override
    public String getName() {
        return this.product.getName();
    }

    @Override
    public ProductPrice getPrice() {
        if ( this.product.getPrice() != null ) {
            return new RestProductPrice(this.product.getPrice());
        }
        return null;
    }

    @Override
    public String getThumbnailUrl() {
        return this.product.getThumbnailUrl();
    }

    static public List<RestProductSummary> toRestList(List<Product> products) {
        ArrayList<RestProductSummary> restProductList = new ArrayList<>();
        products.forEach(product -> restProductList.add(new RestProductSummary(product)));
        return restProductList;
    }

    @Override
    public List<ProductAttribute> getAttributes() {
        return this.attributes.stream().collect(Collectors.toList());
    }

    /************ NOT EXPOSED PROPERTIES ***********/

    @Override
    public String getDescription() {
        return null;
    }

    @Override
    public String getPrimaryImageUrl() {
        return null;
    }

    @Override
    public List<Category> getCategories() {
        return null;
    }

    @Override
    public List<ProductVariant> getVariants() {
        return null;
    }

    @Override
    public List<ProductAttribute> getVariantAttributes() {
        return null;
    }

    @Override
    public List<ProductVariantAttributeType> getVariantAttributeTypes() {
        return null;
    }

    @Override
    public VariantLinkType getVariantLinkType() {
        return null;
    }
}
