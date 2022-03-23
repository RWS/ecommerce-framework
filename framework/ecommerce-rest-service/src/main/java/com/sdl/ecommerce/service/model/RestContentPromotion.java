package com.sdl.ecommerce.service.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.sdl.ecommerce.api.model.ContentPromotion;
import com.sdl.ecommerce.api.model.ImageMapPromotion;
import graphql.annotations.GraphQLDescription;
import graphql.annotations.GraphQLField;
import graphql.annotations.GraphQLName;

import java.util.ArrayList;
import java.util.List;

/**
 * REST Content Promotion
 *
 * @author nic
 */
@GraphQLName("ContentPromotion")
@GraphQLDescription("Content promotion")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RestContentPromotion {

    @GraphQLField
    private String text;

    @GraphQLField
    private String imageUrl;

    @GraphQLField
    private RestLocation location;

    @GraphQLField
    private List<RestContentArea> contentAreas;


    public RestContentPromotion(ContentPromotion contentPromotion) {
        this.text = contentPromotion.getText();
        this.imageUrl = contentPromotion.getImageUrl();
        if ( contentPromotion.getLocation() != null ) {
            this.location = new RestLocation(contentPromotion.getLocation());
        }
        if ( contentPromotion instanceof ImageMapPromotion ) {
            this.contentAreas = new ArrayList<>();
            ((ImageMapPromotion) contentPromotion).getContentAreas().stream().forEach(contentArea -> contentAreas.add(new RestContentArea(contentArea)));
        }
    }

    public String getText() {
        return text;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public RestLocation getLocation() {
        return location;
    }

    public List<RestContentArea> getContentAreas() {
        return contentAreas;
    }
}
