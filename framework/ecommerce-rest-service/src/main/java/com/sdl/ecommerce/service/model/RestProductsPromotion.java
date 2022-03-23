package com.sdl.ecommerce.service.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.sdl.ecommerce.api.model.ProductsPromotion;
import graphql.annotations.GraphQLDescription;
import graphql.annotations.GraphQLField;
import graphql.annotations.GraphQLName;

import java.util.List;

/**
 * REST Products Promotion
 *
 * @author nic
 */
@GraphQLName("ProductsPromotion")
@GraphQLDescription("Promotion with a selected set of products")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RestProductsPromotion {

    @GraphQLField
    private List<RestProductSummary> products;

    public RestProductsPromotion(ProductsPromotion promotion) {
        this.products = RestProductSummary.toRestList(promotion.getProducts());
    }

    public List<RestProductSummary> getProducts() {
        return this.products;
    }

}
