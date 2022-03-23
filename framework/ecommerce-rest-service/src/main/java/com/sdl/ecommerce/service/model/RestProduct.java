package com.sdl.ecommerce.service.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sdl.ecommerce.api.ProductDetailResult;
import com.sdl.ecommerce.api.model.*;
import com.sdl.ecommerce.service.ListHelper;
import graphql.annotations.GraphQLDescription;
import graphql.annotations.GraphQLField;
import graphql.annotations.GraphQLName;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * OData Product
 *
 * @author nic
 */
@GraphQLDescription("E-Commerce Product")
@GraphQLName("Product")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "id", "masterId", "variantId", "name", "description", "price", "thumbnailUrl", "primaryImageUrl", "attributes"})
public class RestProduct implements Product {

    private Product product;

    @GraphQLField
    @GraphQLDescription("Product Category IDs")
    private List<String> categoryIds;

    @GraphQLField
    @GraphQLDescription("Product Price")
    private RestProductPrice price;

    @GraphQLField
    @GraphQLName("attributes")
    private List<RestProductAttribute> attributes;

    @GraphQLField
    @GraphQLDescription("Product breadcrumbs")
    private List<RestBreadcrumb> breadcrumbs;

    @GraphQLField
    @GraphQLDescription("Promotions associated with current product")
    private List<RestPromotion> promotions;

    @GraphQLField
    @GraphQLDescription("Product variants")
    private List<RestProductVariant> productVariants;

    @GraphQLField
    @GraphQLDescription("Product variant attributes")
    private List<RestProductAttribute> variantAttributes;

    private List<RestProductVariantAttributeType> variantAttributeTypes;

    public RestProduct(Product product) {
        this.product = product;
        this.initializeStandardFields();
    }

    public RestProduct(ProductDetailResult detailResult) {
        this.product = detailResult.getProductDetail();

        this.breadcrumbs = ListHelper.createWrapperList(detailResult.getBreadcrumbs(), Breadcrumb.class, RestBreadcrumb.class);
        this.promotions = ListHelper.createWrapperList(detailResult.getPromotions(), Promotion.class, RestPromotion.class);
        this.initializeStandardFields();
    }

    protected void initializeStandardFields() {
        if ( this.product.getPrice() != null ) {
            this.price = new RestProductPrice(this.product.getPrice());
        }
        categoryIds = new ArrayList<>();
        product.getCategories().forEach(category -> categoryIds.add(category.getId()));
        this.attributes = ListHelper.createWrapperList(product.getAttributes(), ProductAttribute.class, RestProductAttribute.class);
        this.productVariants = ListHelper.createWrapperList(product.getVariants(), ProductVariant.class, RestProductVariant.class);
        this.variantAttributes = ListHelper.createWrapperList(product.getVariantAttributes(), ProductAttribute.class, RestProductAttribute.class);
        this.variantAttributeTypes = ListHelper.createWrapperList(product.getVariantAttributeTypes(), ProductVariantAttributeType.class, RestProductVariantAttributeType.class);
    }

    @GraphQLField
    @GraphQLDescription("Product ID")
    @Override
    public String getId() {
        return product.getId();
    }

    @GraphQLField
    @GraphQLDescription("Master ID")
    @Override
    public String getMasterId() {
        return product.getMasterId();
    }

    @GraphQLField
    @GraphQLDescription("Variant ID")
    @Override
    public String getVariantId() {
        return product.getVariantId();
    }

    @GraphQLField
    @GraphQLDescription("Product Name")
    @Override
    public String getName() {
        return product.getName();
    }

    @Override
    public ProductPrice getPrice() {
        return this.price;
    }

    @GraphQLField
    @GraphQLDescription("Product Description")
    @Override
    public String getDescription() {
        return product.getDescription();
    }

    @GraphQLField
    @GraphQLDescription("Thumbnail URL of product image")
    @Override
    public String getThumbnailUrl() {
        return product.getThumbnailUrl();
    }

    @GraphQLField
    @GraphQLDescription("Primary image URL")
    @Override
    public String getPrimaryImageUrl() {
        return product.getPrimaryImageUrl();
    }

    @Override
    public List<Category> getCategories() {
        // Not exposed through REST
        //
        return null;
    }

    public List<String> getCategoryIds() {
        return this.categoryIds;
    }

    @Override
    public List<ProductAttribute> getAttributes() {
        return attributes.stream().collect(Collectors.toList());
    }


    @Override
    public List<ProductVariant> getVariants() {
        return productVariants.stream().collect(Collectors.toList());
    }

    @Override
    public List<ProductAttribute> getVariantAttributes() {
        return variantAttributes.stream().collect(Collectors.toList());
    }

    @Override
    public List<ProductVariantAttributeType> getVariantAttributeTypes() {
        return variantAttributeTypes.stream().collect(Collectors.toList());
    }

    @Override
    public VariantLinkType getVariantLinkType() {
        return product.getVariantLinkType();
    }

    /********* Attributes coming from the query result ***********************/

    public List<Breadcrumb> getBreadcrumbs() {
        return this.breadcrumbs.stream().collect(Collectors.toList());
    }

    public List<Promotion> getPromotions() {
       return this.promotions.stream().collect(Collectors.toList());
    }
}
