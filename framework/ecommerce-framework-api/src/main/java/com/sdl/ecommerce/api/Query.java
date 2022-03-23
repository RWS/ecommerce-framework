package com.sdl.ecommerce.api;

import com.sdl.ecommerce.api.model.Category;
import com.sdl.ecommerce.api.model.FacetParameter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Query
 *
 * Abstract base class for query implementations.
 *
 * @author nic
 */
public abstract class Query implements Cloneable {

    private Category category;
    private List<Category> categories;
    private String searchPhrase;
    private List<FacetParameter> facets;
    private int startIndex;
    private int viewSize;
    private List<QueryFilterAttribute> filterAttributes;
    private ViewType viewType;
    private Map<String,String> contextData;

    // TODO: Add support for sorting
    // TODO: Should this also be an interface???

    /**
     * Set category
     * @param category
     * @return this
     */
    public Query category(Category category) {
        this.category = category;
        return this;
    }

    /**
     * Set a list of categories (OR statement)
     * @param categories
     * @return this
     */
    public Query categories(List<Category> categories) {
        this.categories = categories;
        return this;
    }

    /**
     * Set search phrase
     * @param searchPhrase
     * @return this
     */
    public Query searchPhrase(String searchPhrase) {
        this.searchPhrase = searchPhrase;
        return this;
    }

    /**
     * Set facets
     * @param facets
     * @return this
     */
    public Query facets(List<FacetParameter> facets) {
        if ( this.facets == null ) {
            this.facets = facets;
        }
        else {
            this.facets.addAll(facets);
        }
        return this;
    }

    /**
     * Add a facet
     * @param facet
     * @return this
     */
    public Query facet(FacetParameter facet) {
        if ( this.facets == null ) {
            this.facets = new ArrayList<>();
        }
        this.facets.add(facet);
        return this;
    }

    /**
     * Set start index for the result (used for pagination)
     * @param startIndex
     * @return this
     */
    public Query startIndex(int startIndex) {
        this.startIndex = startIndex;
        return this;
    }

    /**
     * Set view size for the result (used for pagination)
     * @param viewSize
     * @return this
     */
    public Query viewSize(int viewSize) {
        this.viewSize = viewSize;
        return this;
    }

    /**
     * Add a filter attribute to control what information that should be included or excluded.
     * @param filterAttribute
     * @return this
     */
    public Query filterAttribute(QueryFilterAttribute filterAttribute) {
        if ( this.filterAttributes == null ) {
            this.filterAttributes = new ArrayList<>();
        }
        this.filterAttributes.add(filterAttribute);
        return this;
    }

    /**
     * Set view type
     * @param viewType
     * @return this
     */
    public Query viewType(ViewType viewType) {
        this.viewType = viewType;
        return this;
    }

    /**
     * Set context data
     * @param name
     * @param value
     * @return this
     */
    public Query contextData(String name, String value) {
        if (this.contextData == null) {
            this.contextData = new HashMap<>();
        }
        this.contextData.put(name, value);
        return this;
    }

    /**
     * Set context data
     * @param contextData
     * @return this
     */
    public Query contextData(Map<String,String> contextData) {
        if (this.contextData == null) {
            this.contextData = new HashMap<>();
        }
        this.contextData.putAll(contextData);
        return this;
    }

    /**
     * @return category
     */
    public Category getCategory() {
        return category;
    }

    /**
     * @return categories
     */
    public List<Category> getCategories() {
        return categories;
    }

    /**
     * @return list of facets
     */
    public List<FacetParameter> getFacets() {
        return facets;
    }

    /**
     * @return search phrase
     */
    public String getSearchPhrase() {
        return searchPhrase;
    }

    /**
     * @return start index
     */
    public int getStartIndex() {
        return startIndex;
    }

    /**
     * @return view size
     */
    public int getViewSize() {
        return viewSize;
    }

    /**
     * @return list of filter attributes
     */
    public List<QueryFilterAttribute> getFilterAttributes() {
        return filterAttributes;
    }

    /**
     * @return view type
     */
    public ViewType getViewType() {
        return viewType;
    }

    /**
     * @return context data
     */
    public Map<String, String> getContextData() {
        return contextData;
    }

    /**
     * Clone the query
     * @return new cloned instance
     */
    public Query clone() {
        try {
            return (Query) super.clone();
        }
        catch ( CloneNotSupportedException e ) {
            throw new RuntimeException("Could not clone E-Commerce query", e);
        }
    }
}
