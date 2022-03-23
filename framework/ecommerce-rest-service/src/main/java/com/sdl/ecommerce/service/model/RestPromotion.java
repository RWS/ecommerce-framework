package com.sdl.ecommerce.service.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.sdl.ecommerce.api.model.ContentPromotion;
import com.sdl.ecommerce.api.model.ProductsPromotion;
import com.sdl.ecommerce.api.model.Promotion;
import graphql.annotations.GraphQLDescription;
import graphql.annotations.GraphQLField;
import graphql.annotations.GraphQLName;

/**
 * RestPromotion
 *
 * @author nic
 */
@GraphQLName("Promotion")
@GraphQLDescription("E-Commerce Promotion")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RestPromotion implements Promotion {

    @GraphQLField
    private String id;

    @GraphQLField
    private String name;

    @GraphQLField
    private String title;

    @GraphQLField
    private String editUrl;

    // Types of promotion
    //
    @GraphQLField
    private RestProductsPromotion productsPromotion;

    @GraphQLField
    private RestContentPromotion contentPromotion;

    public RestPromotion(Promotion promotion) {
        this.id = promotion.getId();
        this.name = promotion.getName();
        this.title = promotion.getTitle();
        this.editUrl = promotion.getEditUrl();
        if ( promotion instanceof ProductsPromotion ) {
            this.productsPromotion = new RestProductsPromotion((ProductsPromotion) promotion);
        }
        else if ( promotion instanceof ContentPromotion) {
            this.contentPromotion = new RestContentPromotion((ContentPromotion) promotion);
        }
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public String getEditUrl() {
        return editUrl;
    }

    public RestProductsPromotion getProductsPromotion() {
        return productsPromotion;
    }

    public RestContentPromotion getContentPromotion() {
        return contentPromotion;
    }
}
