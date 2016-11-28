package com.sdl.ecommerce.dxa.model;

import com.sdl.ecommerce.api.Query;
import com.sdl.ecommerce.api.QueryInputContributor;
import com.sdl.ecommerce.api.model.FacetGroup;
import com.sdl.ecommerce.api.model.Promotion;
import com.sdl.webapp.common.api.mapping.semantic.annotations.SemanticEntity;
import com.sdl.webapp.common.api.mapping.semantic.annotations.SemanticProperty;
import com.sdl.webapp.common.api.model.entity.AbstractEntityModel;

import java.util.List;

import static com.sdl.webapp.common.api.mapping.semantic.config.SemanticVocabulary.SDL_CORE;

/**
 * Facets Widget
 *
 * @author nic
 */
@SemanticEntity(entityName = "FacetsWidget", vocabulary = SDL_CORE, prefix = "e", public_ = false)
public class FacetsWidget extends AbstractEntityModel implements QueryInputContributor {

    @SemanticProperty("e:category")
    private ECommerceCategoryReference categoryReference;

    @SemanticProperty("e:viewType")
    private String viewType;

    @SemanticProperty("e:filterAttributes")
    private List<ECommerceFilterAttribute> filterAttributes;

    private List<FacetGroup> facetGroups;

    // TODO: Is this the best way of doing this???
    private List<Promotion> relatedPromotions;

    public ECommerceCategoryReference getCategoryReference() {
        return categoryReference;
    }

    public String getViewType() {
        return viewType;
    }

    public List<ECommerceFilterAttribute> getFilterAttributes() {
        return filterAttributes;
    }

    public List<FacetGroup> getFacetGroups() {
        return facetGroups;
    }

    public void setFacetGroups(List<FacetGroup> facetGroups) {
        this.facetGroups = facetGroups;
    }

    public List<Promotion> getRelatedPromotions() {
        return relatedPromotions;
    }

    public void setRelatedPromotions(List<Promotion> relatedPromotions) {
        this.relatedPromotions = relatedPromotions;
    }

    public String getPromotionViewName(Promotion promotion) {
        return promotion.getClass().getInterfaces()[0].getSimpleName();
    }

    @Override
    public void contributeToQuery(Query query) {
        if ( filterAttributes != null ) {
            for ( ECommerceFilterAttribute filterAttribute : filterAttributes ) {
                query.filterAttribute(filterAttribute.toQueryFilterAttribute());
            }
        }
    }
}
