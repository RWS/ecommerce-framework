package com.sdl.ecommerce.service.model;

import com.sdl.ecommerce.api.model.ImageMapPromotion;
import com.sdl.ecommerce.api.model.Location;
import graphql.annotations.GraphQLDescription;
import graphql.annotations.GraphQLField;
import graphql.annotations.GraphQLName;

/**
 * RestContentArea
 *
 * @author nic
 */
@GraphQLName("ContentArea")
@GraphQLDescription("Image map content area")
public class RestContentArea {

    @GraphQLField
    private int x1;

    @GraphQLField
    private int y1;

    @GraphQLField
    private int x2;

    @GraphQLField
    private int y2;

    @GraphQLField
    private RestLocation location;

    public RestContentArea(ImageMapPromotion.ContentArea contentArea) {
        this.x1 = contentArea.getX1();
        this.y1 = contentArea.getY1();
        this.x2 = contentArea.getX2();
        this.y2 = contentArea.getY2();
        this.location = new RestLocation(contentArea.getLocation());
    }

    public int getX1() {
        return x1;
    }

    public int getY1() {
        return y1;
    }

    public int getX2() {
        return x2;
    }

    public int getY2() {
        return y2;
    }

    public Location getLocation() {
        return location;
    }

}
