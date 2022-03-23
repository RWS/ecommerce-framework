package com.sdl.ecommerce.service.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.sdl.ecommerce.api.ECommerceException;
import com.sdl.ecommerce.api.Query;
import com.sdl.ecommerce.api.QueryResult;
import com.sdl.ecommerce.api.model.*;
import com.sdl.ecommerce.service.ListHelper;
import graphql.annotations.GraphQLDescription;
import graphql.annotations.GraphQLField;
import graphql.annotations.GraphQLName;

import java.util.List;
import java.util.stream.Collectors;

/**
 * REST Query Result
 *
 * @author nic
 */
@GraphQLName("ProductQueryResult")
@GraphQLDescription("E-Commerce Product Query Result")
@JsonInclude(JsonInclude.Include.NON_NULL)

// TODO: Return full product when Graph-QL!!!!
public class RestQueryResult implements QueryResult {

    private QueryResult queryResult;

    @GraphQLField
    private List<RestProductSummary> products;

    @GraphQLField
    private List<RestFacetGroup> facetGroups;

    @GraphQLField
    private List<RestPromotion> promotions;

    @GraphQLField
    private List<RestBreadcrumb> breadcrumbs;

    public RestQueryResult(QueryResult queryResult) {
        this.queryResult = queryResult;
        this.products = ListHelper.createWrapperList(queryResult.getProducts(), Product.class, RestProductSummary.class);
        this.facetGroups = ListHelper.createWrapperList(queryResult.getFacetGroups(), FacetGroup.class, RestFacetGroup.class);
        this.promotions = ListHelper.createWrapperList(queryResult.getPromotions(), Promotion.class, RestPromotion.class);
        this.breadcrumbs = ListHelper.createWrapperList(queryResult.getBreadcrumbs(), Breadcrumb.class, RestBreadcrumb.class);
    }

    @Override
    public Category getCategory() {
        if ( queryResult.getCategory() != null ) {
            return new RestCategory(queryResult.getCategory());
        }
        return null;
    }

    @Override
    public List<Product> getProducts() {
        return this.products.stream().collect(Collectors.toList());
    }

    @Override
    public List<FacetGroup> getFacetGroups() {
        return this.facetGroups.stream().collect(Collectors.toList());
    }

    @GraphQLField
    @Override
    public int getTotalCount() {
        return queryResult.getTotalCount();
    }

    @GraphQLField
    @Override
    public int getStartIndex() {
        return queryResult.getStartIndex();
    }

    @GraphQLField
    @Override
    public int getViewSize() {
        return queryResult.getViewSize();
    }

    @GraphQLField
    @Override
    public int getCurrentSet() {
        return queryResult.getCurrentSet();
    }

    @Override
    public List<QuerySuggestion> getQuerySuggestions() {
        return queryResult.getQuerySuggestions();
    }

    @Override
    public QueryResult next() throws ECommerceException {
        return null;
    }

    @Override
    public QueryResult previous() throws ECommerceException {
        return null;
    }

    @Override
    public Location getRedirectLocation() {
        if ( queryResult.getRedirectLocation() != null ) {
            return new RestLocation(queryResult.getRedirectLocation());
        }
        return null;
    }

    @Override
    public Query getQuery() {
        return null;
    }

    @Override
    public List<Breadcrumb> getBreadcrumbs() {
         return breadcrumbs.stream().collect(Collectors.toList());
    }

    @Override
    public List<Promotion> getPromotions() {
        return promotions.stream().collect(Collectors.toList());
    }


}
