package com.sdl.ecommerce.api.test;

import com.sdl.ecommerce.api.ECommerceLinkResolver;
import com.sdl.ecommerce.api.model.*;

import java.util.List;

/**
 * Link resolver for unit tests with some dummy implementations of the different methods
 *
 * @author nic
 */
public class TestLinkResolver implements ECommerceLinkResolver {

    @Override
    public String getCategoryLink(Category category) {
        return CategoryRef.getCategoryAbsolutePath(category);
    }

    @Override
    public String getNonContextualCategoryLink(Category category) {
        return CategoryRef.getCategoryAbsolutePath(category);
    }

    @Override
    public String getFacetLink(Facet facet) {
        if ( facet.isCategory() ) {
            return "/" + facet.getTitle().toLowerCase().replace(" ", "_");
        }
        else {
            return "?" + facet.getId() + "=" + facet.getValue();
        }
    }

    @Override
    public String getAbsoluteFacetLink(Facet facet, String navigationBasePath) {
        return navigationBasePath + getFacetLink(facet);
    }

    @Override
    public String getBreadcrumbLink(Breadcrumb breadcrumb) {
        return null;
    }

    @Override
    public String getLocationLink(Location location) {
        if ( location.getStaticUrl() != null ) {
            return location.getStaticUrl();
        }
        if ( location.getProductRef() != null ) {
            return "/p/" + location.getProductRef().getId();
        }
        StringBuilder link = new StringBuilder();
        if ( location.getCategoryRef() != null ) {
            link.append(location.getCategoryRef().getPath());
        }
        if ( location.getFacets() != null && !location.getFacets().isEmpty() ) {
            link.append("?");
            for ( int i=0; i < location.getFacets().size(); i++ ) {
                FacetParameter facet = location.getFacets().get(i);
                link.append(facet.toUrl());
                if ( i+1 < location.getFacets().size() ) {
                    link.append("&");
                }
            }
        }
        return link.toString();
    }

    @Override
    public String getProductVariantDetailLink(Product product) {
        return "/p/" + product.getId();
    }

    @Override
    public String getProductDetailLink(Product product) {
        return "/p/" + product.getId();
    }

    @Override
    public String getProductDetailVariantLink(Product product, String variantAttributeId, String variantAttributeValueId) {
        return "/p/" + product.getId();
    }
}
