package com.sdl.ecommerce.fredhopper;

import com.fredhopper.webservice.client.*;
import com.sdl.ecommerce.api.*;
import com.sdl.ecommerce.api.ViewType;
import com.sdl.ecommerce.api.model.*;
import com.sdl.ecommerce.api.model.QuerySuggestion;
import com.sdl.ecommerce.api.model.impl.GenericLocation;
import com.sdl.ecommerce.fredhopper.model.*;
import com.sdl.ecommerce.fredhopper.model.promotion.FredhopperPromotion;

import java.net.URLDecoder;
import java.util.*;

/**
 * Fredhopper Result Set
 *
 * @author nic
 */
public class FredhopperQueryResult extends FredhopperResultBase implements QueryResult {

    private Query query;
    private Category category;

    public FredhopperQueryResult(Page fredhopperPage, Query query, FredhopperLinkManager linkManager) {
        super(fredhopperPage, linkManager);
        this.query = query;
        this.category = query.getCategory();

        if ( query.getViewType() == ViewType.FLYOUT && query.getFilterAttributes() == null ) {
            // Add default query filter attribute for flyouts
            //
            query.filterAttribute(new QueryFilterAttribute("flyout", "yes"));
        }

    }

    @Override
    public Category getCategory() {
        return this.category;
    }

    @Override
    public List<Product> getProducts() {
        if (universe != null) {
            ItemsSection itemsSection = universe.getItemsSection();
            if (itemsSection != null) {
                return this.getProducts(itemsSection.getItems().getItem());
            }
        }
        return new ArrayList<>();
    }

    @Override
    public List<Promotion> getPromotions() {
        return this.getPromotions(this.universe, this.query.getFilterAttributes());
    }

    @Override
    public List<FacetGroup> getFacetGroups() {
        return this.getFacetGroups(this.universe, this.query.getFilterAttributes());
    }

    @Override
    public List<Breadcrumb> getBreadcrumbs() {
        return this.getBreadcrumbs(this.universe, query.getFacets());
    }

    @Override
    public int getTotalCount() {
        if ( this.universe != null && this.universe.getItemsSection() != null && this.universe.getItemsSection().getResults() != null ) {
            return this.universe.getItemsSection().getResults().getTotalItems();
        }
        else {
            return 0;
        }
    }

    @Override
    public int getCurrentSet() {
        if ( this.universe != null && this.universe.getItemsSection() != null && this.universe.getItemsSection().getResults() != null ) {
            return this.universe.getItemsSection().getResults().getCurrentSet();
        }
        else {
            return 0;
        }
    }

    @Override
    public int getStartIndex() {
        if ( this.universe != null && this.universe.getItemsSection() != null && this.universe.getItemsSection().getResults() != null ) {
            return this.universe.getItemsSection().getResults().getStartIndex();
        }
        else {
            return 0;
        }
    }

    @Override
    public int getViewSize() {
        if ( this.universe != null && this.universe.getItemsSection() != null  && this.universe.getItemsSection().getResults() != null ) {
            return this.universe.getItemsSection().getResults().getViewSize();
        }
        else {
            return 0;
        }
    }

    @Override
    public List<QuerySuggestion> getQuerySuggestions() {
        List<QuerySuggestion> querySuggestions =null;
        if ( this.universe != null && this.universe.getQueryAlternatives() != null ) {
            querySuggestions = new ArrayList<>();
            for (com.fredhopper.webservice.client.QuerySuggestion querySuggestion : this.universe.getQueryAlternatives().getQuerySuggestion() ) {
                querySuggestions.add(new FredhopperQuerySuggestion(querySuggestion));
            }
        }
        return querySuggestions;
    }

    @Override
    public QueryResult next() throws ECommerceException {
        final Query nextQuery = this.query.clone();
        nextQuery.startIndex(nextQuery.getStartIndex()+nextQuery.getViewSize());
        return this.queryService.query(nextQuery);
    }

    @Override
    public QueryResult previous() throws ECommerceException {
        final Query previousQuery = this.query.clone();
        previousQuery.startIndex(previousQuery.getStartIndex()-previousQuery.getViewSize());
        return this.queryService.query(previousQuery);
    }

    @Override
    public Location getRedirectLocation() {
        if ( this.fredhopperPage.getRedirect() != null ) {
            return new GenericLocation(this.fredhopperPage.getRedirect().getRedirectUrl());
        }
        if ( this.fredhopperPage.getInfo().getView() == com.fredhopper.webservice.client.ViewType.DETAIL ) {
            List<Product> products = this.getProducts();
            if ( products.size() == 1 ) {

                return new GenericLocation(products.get(0));
            }
        }
        return null;
    }

    @Override
    public Query getQuery() {
        return this.query;
    }
}
