package com.sdl.ecommerce.api.model;

import java.util.List;

/**
 * Facet Group.
 * Represents a set of facets (e.g. brand, color, size etc).
 *
 * @author nic
 */
public interface FacetGroup extends Editable {

    /**
     * Get unique identifier for this facet group
     * @return id
     */
    String getId();

    /**
     * Get title of the facet group. Should be localized to current language.
     * @return
     */
    String getTitle();

    /**
     * Get type of facet group.
     * @return type
     */
    String getType();   // TODO: Have a standardised enum type here for the facet type (is not used right now anyway)

    /**
     * Get all belonging facets
     * @return facets
     */
    List<Facet> getFacets();

    /**
     * Indicate if current facet group represents a product category or not.
     * @return is category
     */
    boolean isCategory();

    /**
     * Get associated attributes to control presentation of facets etc.
     * @return attributes
     */
    List<NameValue> getAttributes();
}
