package com.sdl.ecommerce.dxa.controller;

import com.sdl.ecommerce.api.*;

import static com.sdl.ecommerce.dxa.ECommerceRequestAttributes.*;

import com.sdl.ecommerce.api.edit.EditMenu;
import com.sdl.ecommerce.api.edit.EditService;
import com.sdl.ecommerce.api.edit.MenuItem;
import com.sdl.ecommerce.api.model.*;
import com.sdl.ecommerce.dxa.CategoryDataCache;
import com.sdl.ecommerce.dxa.ECommerceViewHelper;
import com.sdl.ecommerce.dxa.model.*;
import com.sdl.webapp.common.api.WebRequestContext;
import com.sdl.webapp.common.api.content.ContentProviderException;
import com.sdl.webapp.common.api.model.MvcData;
import com.sdl.webapp.common.controller.BaseController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * Widget Controller
 *
 * @author nic
 */
@Controller
@RequestMapping("/system/mvc/ECommerce/WidgetController")
public class WidgetController extends BaseController {

    @Autowired
    private ProductCategoryService categoryService;

    @Autowired
    private ProductQueryService queryService;

    @Autowired
    private ECommerceViewHelper viewHelper;

    @Autowired
    private CategoryDataCache categoryDataCache;

    @Autowired
    private WebRequestContext webRequestContext;

    @Autowired(required = false)
    private EditService editService;

    static class FlyoutData {
        List<FacetGroup> facetGroups;
        List<Promotion> promotions;
    }

    // TODO: Rename to ProductLister
    @RequestMapping(method = RequestMethod.GET, value = "ItemLister/{entityId}")
    public String handleItemLister(HttpServletRequest request, @PathVariable String entityId) throws ContentProviderException {

        ItemListerWidget entity = (ItemListerWidget) this.getEntityFromRequest(request, entityId);

        QueryResult queryResult = this.getQueryResult(request);
        entity.setItems(queryResult.getProducts());
        this.processListerNavigationLinks(entity, queryResult, this.getFacets(request));

        request.setAttribute("entity", entity);

        final MvcData mvcData = entity.getMvcData();
        return resolveView(mvcData, "Entity", request);
    }

    @RequestMapping(method = RequestMethod.GET, value = "Facets/{entityId}")
    public String handleFacets(HttpServletRequest request, @PathVariable String entityId) throws ContentProviderException {

        FacetsWidget entity = (FacetsWidget) this.getEntityFromRequest(request, entityId);

        QueryResult queryResult = this.getQueryResult(request);
        entity.setFacetGroups(queryResult.getFacetGroups(this.getUrlPrefix(request)));

        request.setAttribute("entity", entity);
        request.setAttribute("viewHelper", this.viewHelper);
        this.buildInContextControls(request);

        final MvcData mvcData = entity.getMvcData();
        return resolveView(mvcData, "Entity", request);
    }

    @RequestMapping(method = RequestMethod.GET, value = "Breadcrumb/{entityId}")
    public String handleBreadcrumb(HttpServletRequest request, @PathVariable String entityId) throws ContentProviderException {

        BreadcrumbWidget entity = (BreadcrumbWidget) this.getEntityFromRequest(request, entityId);

        ECommerceResult result = this.getResult(request);
        entity.setBreadcrumbs(result.getBreadcrumbs(this.getUrlPrefix(request), this.getRootCategoryTitle(request)));
        if ( result instanceof QueryResult ) {
            entity.setTotalItems(((QueryResult) result).getTotalCount());
        }

        request.setAttribute("entity", entity);

        final MvcData mvcData = entity.getMvcData();
        return resolveView(mvcData, "Entity", request);
    }

    @RequestMapping(method = RequestMethod.GET, value = "Promotions/{entityId}")
    public String handlePromotions(HttpServletRequest request, @PathVariable String entityId) throws ContentProviderException {

        PromotionsWidget entity = (PromotionsWidget) this.getEntityFromRequest(request, entityId);

        /* TODO: Fix functionality to overload categories via widget metadata
        if ( entity.getCategory() != null && entity.getPageType() == null ) {
            Page page = this.fredhopperService.queryByCategoryPath(entity.getCategory());
            fhUniverse =fredhopperService.getUniverse(page);
        }
        else if ( entity.getPageType() != null ) {
            Page page = this.fredhopperService.queryByCategoryPath(entity.getCategory(), ViewType.parse(entity.getPageType()));
            fhUniverse =fredhopperService.getUniverse(page);
        }
        else {
            fhUniverse = this.getFredhopperUniverse(request);
        }
        */

        ECommerceResult result = this.getResult(request);
        entity.setPromotions(result.getPromotions());

        request.setAttribute("entity", entity);
        request.setAttribute("viewHelper", this.viewHelper);

        final MvcData mvcData = entity.getMvcData();
        return resolveView(mvcData, "Entity", request);
    }

    @RequestMapping(method = RequestMethod.GET, value = "FlyoutFacets/{entityId}")
    public String handleFlyoutFacets(HttpServletRequest request, @PathVariable String entityId) throws ContentProviderException {

        FacetsWidget entity = (FacetsWidget) this.getEntityFromRequest(request, entityId);

        Category topCategory = this.categoryService.getCategoryByPath(entity.getCategoryPath());
        if ( topCategory != null ) {
            entity.setCategory(topCategory);
            entity.setCategoryUrl(topCategory.getCategoryLink("/c")); // For flyout is the URL's always based on the category url pattern

            boolean useCache = this.getSessionPreviewToken(request) == null;

            FlyoutData flyoutData = null;
            if (useCache) {
                flyoutData = (FlyoutData) this.categoryDataCache.getCategoryData(topCategory, "flyout");
            }
            if (flyoutData == null) {
                QueryResult flyoutResult = this.queryService.query(
                        this.queryService.newQuery().
                                category(topCategory).
                                viewType(ViewType.FLYOUT));
                flyoutData = new FlyoutData();
                flyoutData.facetGroups = flyoutResult.getFacetGroups("/c");
                flyoutData.promotions = flyoutResult.getPromotions();
                this.categoryDataCache.setCategoryData(topCategory, "flyout", flyoutData);
            }

            entity.setFacetGroups(flyoutData.facetGroups);
            entity.setRelatedPromotions(flyoutData.promotions);
        }
        request.setAttribute("entity", entity);
        request.setAttribute("viewHelper", this.viewHelper);

        final MvcData mvcData = entity.getMvcData();
        return resolveView(mvcData, "Entity", request);
    }

    @RequestMapping(method = RequestMethod.GET, value = "SearchFeedback/{entityId}")
    public String handleSearchFeedback(HttpServletRequest request, @PathVariable String entityId) throws ContentProviderException {

        SearchFeedbackWidget entity = (SearchFeedbackWidget) this.getEntityFromRequest(request, entityId);
        QueryResult queryResult = this.getQueryResult(request);
        entity.setQuerySuggestions(queryResult.getQuerySuggestions());
        request.setAttribute("entity", entity);
        final MvcData mvcData = entity.getMvcData();
        return resolveView(mvcData, "Entity", request);
    }

    @RequestMapping(method = RequestMethod.GET, value = "ProductDetail/{entityId}")
    public String handleProductDetail(HttpServletRequest request, @PathVariable String entityId) throws ContentProviderException {
        ProductDetailWidget entity = (ProductDetailWidget) this.getEntityFromRequest(request, entityId);

        Product product = (Product) request.getAttribute(PRODUCT);
        if ( product == null ) {
            throw new ContentProviderException("No product found!");
        }
        entity.setProduct(product);
        request.setAttribute("entity", entity);

        final MvcData mvcData = entity.getMvcData();
        return resolveView(mvcData, "Entity", request);
    }

    @RequestMapping(method = RequestMethod.GET, value = "DetailBreadcrumb/{entityId}")
    public String handleDetailBreadcrumb(HttpServletRequest request, @PathVariable String entityId) throws ContentProviderException {
        // TODO: REMOVE THIS METHOD! IS NOT NEEDED ANYMORE...
        return this.handleBreadcrumb(request, entityId);
    }

    protected QueryResult getQueryResult(HttpServletRequest request) {
        return (QueryResult) request.getAttribute(RESULT);
    }

    protected ECommerceResult getResult(HttpServletRequest request) {
        return (ECommerceResult) request.getAttribute(RESULT);
    }

    protected Category getCategory(HttpServletRequest request) {
        return (Category) request.getAttribute(CATEGORY);
    }

    protected String getUrlPrefix(HttpServletRequest request) {
        String urlPrefix = (String) request.getAttribute(URL_PREFIX);
        if ( urlPrefix == null ) {
            urlPrefix = "/c"; // fallback to default category URL prefix
        }
        return urlPrefix;
    }

    protected String getRootCategoryTitle(HttpServletRequest request) {
        String rootCategoryTitle= (String) request.getAttribute(ROOT_CATEGORY_TITLE);
        /*if ( rootCategoryTitle == null ) {
            rootCategoryTitle = "Store"; // fallback to default
        } */
        return rootCategoryTitle;
    }

    protected List<FacetParameter> getFacets(HttpServletRequest request) {
        List<FacetParameter> facets = (List<FacetParameter>) request.getAttribute(FACETS);
        return facets;
    }

    protected String getSessionPreviewToken(HttpServletRequest request) {
        if(request == null) {
            return null;
        }
        else {
            Cookie[] cookies = request.getCookies();
            if(cookies != null) {
                Cookie[] var2 = cookies;
                int var3 = cookies.length;

                for(int var4 = 0; var4 < var3; ++var4) {
                    Cookie cookie = var2[var4];
                    if("preview-session-token".equals(cookie.getName())) {
                        return cookie.getValue();
                    }
                }
            }
            return null;
        }
    }

    protected void processListerNavigationLinks(ItemListerWidget lister, QueryResult result, List<FacetParameter> facets) {

        int totalCount = result.getTotalCount();
        int viewSize = result.getViewSize();
        int startIndex = result.getStartIndex();
        int currentSet = result.getCurrentSet();

        int viewSets = 1;
        if ( totalCount > viewSize ) {
            viewSets = (totalCount / viewSize) + 1;
        }
        lister.setViewSets(viewSets);

        if ( viewSets > 1 ) {
            int nextStartIndex = -1;
            int previousStartIndex = -1;
            if ( currentSet > 1 ) {
                previousStartIndex = startIndex-viewSize;
            }
            if ( currentSet < viewSets ) {
                nextStartIndex = startIndex+viewSize;
            }
            String baseUrl = this.getFacetLink(facets);
            if ( baseUrl.isEmpty() ) {
                baseUrl += "?";
            }
            else {
                baseUrl += "&";
            }

            // TODO: Can it be dangerous to update entities here? Are we updating cache objects???

            if ( previousStartIndex != -1 ) {
                lister.setPreviousUrl(baseUrl + "startIndex=" + previousStartIndex);
            }
            if ( nextStartIndex != -1 ) {
                lister.setNextUrl(baseUrl + "startIndex=" + nextStartIndex);
            }
            if ( currentSet > 2 ) {
                lister.setFirstUrl(baseUrl + "startIndex=0");
            }
            if ( currentSet+1 < viewSets ) {
                lister.setLastUrl(baseUrl + "startIndex=" + ((viewSets-1) * viewSize));
            }
            lister.setCurrentSet(currentSet);
            lister.setShowNavigation(true);
        }
        else {
            lister.setShowNavigation(false);
        }

    }

    protected String getFacetLink(List<FacetParameter> facets) {

        if ( facets == null || facets.size() == 0 ) { return ""; }
        StringBuilder sb = new StringBuilder();
        boolean firstParam = true;
        for ( FacetParameter facet : facets ) {
            if ( firstParam ) {
                sb.append("?");
                firstParam = false;
            }
            else {
                sb.append("&");
            }
            sb.append(facet.toUrl());
        }
        return sb.toString();
    }

    protected void buildInContextControls(HttpServletRequest request) {

        // TODO: Refactor this one. The Edit Service should return all available edit menues
        //
        Category category = this.getCategory(request);
        if ( category != null && this.editService != null ) {
            EditMenu editMenu = this.editService.getInContextMenuItems(category, EditService.MenuType.CREATE_NEW, this.webRequestContext.getLocalization());
            request.setAttribute("editMenu", editMenu);
        }


    }

}
