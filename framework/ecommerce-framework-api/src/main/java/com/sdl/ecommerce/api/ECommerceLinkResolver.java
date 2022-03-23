package com.sdl.ecommerce.api;

import com.sdl.ecommerce.api.model.*;

import java.util.List;

/**
 * E-Commerce Link Resolver
 *
 * @author nic
 */
public interface ECommerceLinkResolver {

    /**
     * Get link to category page.
     *
     * @param category
     * @return link
     */
    String getCategoryLink(Category category);

    /**
     * Get a non-contextual category link which is not dependent if a category is within a search or lister context.
     * Can be used for render category links in top navigations etc.
     * @param category
     * @return
     */
    String getNonContextualCategoryLink(Category category);

    /**
     * Get facet link.
     * @param facet
     * @return link
     */
    String getFacetLink(Facet facet);

    /**
     * Get an absolute facet link using a base category path (for flyouts etc).
     * @param facet
     * @param navigationBasePath
     * @return link
     */
    String getAbsoluteFacetLink(Facet facet, String navigationBasePath);

    /**
     * Get breadcrumb link
     * @param breadcrumb
     * @return link
     */
    String getBreadcrumbLink(Breadcrumb breadcrumb);

    /**
     * Get link to location, normally a category + a set of facets)
     * @param location
     * @return location
     */
    String getLocationLink(Location location);

    /**
     * Get link to product detail page.
     * @param product
     * @return link
     */
    String getProductDetailLink(Product product);

    /**
     * Get link to product detail variant page.
     * @param product
     * @return
     */
    String getProductVariantDetailLink(Product product);

    /**
     * Get link to product detail variant page.
     * @param product
     * @param variantAttributeId
     * @param  variantAttributeValueId
     * @return link
     */
    String getProductDetailVariantLink(Product product, String variantAttributeId, String variantAttributeValueId);
}
