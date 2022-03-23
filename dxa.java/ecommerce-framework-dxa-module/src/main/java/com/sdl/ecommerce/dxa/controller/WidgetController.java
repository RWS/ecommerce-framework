package com.sdl.ecommerce.dxa.controller;

import com.sdl.ecommerce.api.*;

import static com.sdl.ecommerce.dxa.ECommerceRequestAttributes.*;

import com.sdl.ecommerce.api.edit.EditMenu;
import com.sdl.ecommerce.api.edit.EditService;
import com.sdl.ecommerce.api.model.*;
import com.sdl.ecommerce.dxa.CategoryDataCache;
import com.sdl.ecommerce.dxa.ECommerceSessionAttributes;
import com.sdl.ecommerce.dxa.ECommerceViewHelper;
import com.sdl.ecommerce.dxa.model.*;
import com.sdl.webapp.common.api.WebRequestContext;
import com.sdl.webapp.common.api.content.ContentProviderException;
import com.sdl.webapp.common.api.model.*;
import com.sdl.webapp.common.controller.BaseController;
import static com.sdl.webapp.common.controller.RequestAttributeNames.PAGE_MODEL;

import com.sdl.webapp.common.controller.exception.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

/**
 * Widget Controller.
 * Manage all E-Commerce widgets for facets, promotions, product details etc.
 *
 * @author nic
 */
@Controller
@RequestMapping("/system/mvc/ECommerce/EComWidget")
public class WidgetController extends BaseController {

    private static final Logger LOG = LoggerFactory.getLogger(WidgetController.class);

    @Autowired
    private ProductCategoryService categoryService;

    @Autowired
    private ProductQueryService queryService;

    @Autowired
    private ProductDetailService detailService;

    @Autowired
    private ECommerceViewHelper viewHelper;

    @Autowired
    private CategoryDataCache categoryDataCache;

    @Autowired
    private WebRequestContext webRequestContext;

    @Autowired
    private ECommerceLinkResolver linkResolver;

    @Autowired(required = false)
    private EditService editService;

    /* TODO: Use thread local to optimize number of category/product lookups
    @Autowired
    private ThreadLocalManager threadLocalManager;
    private ThreadLocal
    */

    /**
     * Holder class for flyout data that are cached
     */
    static class FlyoutData {
        List<FacetGroup> facetGroups;
        List<Promotion> promotions;
    }

    /**
     * Handle product lister.
     * @param request
     * @param entityId
     * @return view
     * @throws ContentProviderException
     */
    @RequestMapping(method = RequestMethod.GET, value = "ProductLister/{entityId}")
    public String handleProductLister(HttpServletRequest request, @PathVariable String entityId) throws ContentProviderException {

        ProductListerWidget entity = (ProductListerWidget) this.getEntityFromRequest(request, entityId);
        QueryResult queryResult;
        if ( entity.getCategoryReference() != null ) {
            Category category = this.resolveCategoryModel(entity.getCategoryReference());
            if ( category == null ) {
                throw new ContentProviderException("Invalid E-Commerce category set for item lister: " + entityId);
            }
            Query query = this.queryService.newQuery();
            query.category(category);
            if ( entity.getViewSize() != 0 ) {
                query.viewSize(entity.getViewSize());
            }
            if ( entity.getViewType() != null ) {
                query.viewType(ViewType.valueOf(entity.getViewType().toUpperCase()));
            }
            query.startIndex(this.getStartIndex(request));
            queryResult = this.queryService.query(query);
        }
        else {
            queryResult = this.getQueryResult(request);
        }
        entity.setItems(queryResult.getProducts());
        this.processListerNavigationLinks(entity, queryResult, this.getFacets(request));

        request.setAttribute("entity", entity);
        request.setAttribute("linkResolver", this.linkResolver);

        final MvcData mvcData = entity.getMvcData();
        return resolveView(mvcData, "Entity", request);
    }

    /**
     * Handle facets.
     * @param request
     * @param entityId
     * @return view
     * @throws ContentProviderException
     */
    @RequestMapping(method = RequestMethod.GET, value = "Facets/{entityId}")
    public String handleFacets(HttpServletRequest request, @PathVariable String entityId) throws ContentProviderException {

        // TODO: Generate the URLs to facets here!!!

        FacetsWidget entity = (FacetsWidget) this.getEntityFromRequest(request, entityId);

        QueryResult queryResult;
        if ( entity.getCategoryReference() != null ) {
            Category category = this.resolveCategoryModel(entity.getCategoryReference());
            if ( category == null ) {
                throw new ContentProviderException("Invalid E-Commerce category set for facet: " + entityId);
            }
            Query query = this.queryService.newQuery();
            query.category(category);
            if ( entity.getViewType() != null ) {
                query.viewType(ViewType.valueOf(entity.getViewType().toUpperCase()));
            }
            queryResult = this.queryService.query(query);
        }
        else {
            queryResult = this.getQueryResult(request);
        }

        entity.setFacetGroups(queryResult.getFacetGroups());

        request.setAttribute("entity", entity);
        request.setAttribute("viewHelper", this.viewHelper);
        request.setAttribute("linkResolver", this.linkResolver);
        this.buildInContextControls(request);

        final MvcData mvcData = entity.getMvcData();
        return resolveView(mvcData, "Entity", request);
    }

    /**
     * Handle breadcrumb.
     * @param request
     * @param entityId
     * @return view
     * @throws ContentProviderException
     */
    @RequestMapping(method = RequestMethod.GET, value = "Breadcrumb/{entityId}")
    public String handleBreadcrumb(HttpServletRequest request, @PathVariable String entityId) throws ContentProviderException {

        BreadcrumbWidget entity = (BreadcrumbWidget) this.getEntityFromRequest(request, entityId);
        ECommerceResult result;
        if ( entity.getCategoryReference() != null ) {
            Category category = this.resolveCategoryModel(entity.getCategoryReference());
            if ( category == null ) {
                throw new ContentProviderException("Invalid E-Commerce category set for breadcrumb: " + entityId);
            }
            Query query = this.queryService.newQuery();
            query.category(category);
            result = this.queryService.query(query);
        }
        else if ( entity.getProductReference() != null ) {
            result = this.resolveProductDetail(entity.getProductReference());
        }
        else {
            result = this.getResult(request);
        }
        entity.setBreadcrumbs(result.getBreadcrumbs()); // this.getUrlPrefix(request), this.getRootCategoryTitle(request)));
        if ( result instanceof QueryResult ) {
            entity.setTotalItems(((QueryResult) result).getTotalCount());
        }

        request.setAttribute("entity", entity);
        request.setAttribute("linkResolver", this.linkResolver);

        final MvcData mvcData = entity.getMvcData();
        return resolveView(mvcData, "Entity", request);
    }

    /**
     * Handle promotions.
     * @param request
     * @param entityId
     * @return view
     * @throws ContentProviderException
     */
    @RequestMapping(method = RequestMethod.GET, value = "Promotions/{entityId}")
    public String handlePromotions(HttpServletRequest request, @PathVariable String entityId) throws ContentProviderException {

        PromotionsWidget entity = (PromotionsWidget) this.getEntityFromRequest(request, entityId);

        this.getPromotions(entity, request);

        request.setAttribute("entity", entity);
        request.setAttribute("viewHelper", this.viewHelper);
        request.setAttribute("linkResolver", this.linkResolver);

        final MvcData mvcData = entity.getMvcData();
        return resolveView(mvcData, "Entity", request);
    }

    /**
     * Handle flyout facets and associated promotions.
     * @param request
     * @param entityId
     * @return view
     * @throws ContentProviderException
     */
    @RequestMapping(method = RequestMethod.GET, value = "FlyoutFacets/{entityId}")
    public String handleFlyoutFacets(HttpServletRequest request, @PathVariable String entityId) throws ContentProviderException {

        FacetsWidget entity = (FacetsWidget) this.getEntityFromRequest(request, entityId);

        if ( entity.getCategoryReference() != null ) {
            Category topCategory = this.resolveCategoryModel(entity.getCategoryReference());
            if (topCategory != null) {
                entity.getCategoryReference().setCategoryUrl(this.linkResolver.getNonContextualCategoryLink(topCategory)); // For flyout is the URL's always based on the category url pattern

                boolean useCache = this.isSessionPreview(request) == false;

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
                    flyoutData.facetGroups = flyoutResult.getFacetGroups();
                    flyoutData.promotions = flyoutResult.getPromotions();
                    this.categoryDataCache.setCategoryData(topCategory, "flyout", flyoutData);
                }

                entity.setFacetGroups(flyoutData.facetGroups);
                entity.setRelatedPromotions(flyoutData.promotions);
            }
        }
        request.setAttribute("entity", entity);
        request.setAttribute("viewHelper", this.viewHelper);
        request.setAttribute("linkResolver", this.linkResolver);

        final MvcData mvcData = entity.getMvcData();
        return resolveView(mvcData, "Entity", request);
    }

    /**
     * Handle search feedback.
     * @param request
     * @param entityId
     * @return view
     * @throws ContentProviderException
     */
    @RequestMapping(method = RequestMethod.GET, value = "SearchFeedback/{entityId}")
    public String handleSearchFeedback(HttpServletRequest request, @PathVariable String entityId) throws ContentProviderException {

        SearchFeedbackWidget entity = (SearchFeedbackWidget) this.getEntityFromRequest(request, entityId);
        QueryResult queryResult = this.getQueryResult(request);
        entity.setQuerySuggestions(queryResult.getQuerySuggestions());
        request.setAttribute("entity", entity);
        final MvcData mvcData = entity.getMvcData();
        return resolveView(mvcData, "Entity", request);
    }

    /**
     * Handle product detail.
     * @param request
     * @param entityId
     * @return view
     * @throws ContentProviderException
     */
    @RequestMapping(method = RequestMethod.GET, value = "ProductDetail/{entityId}")
    public String handleProductDetail(HttpServletRequest request, @PathVariable String entityId) throws ContentProviderException {
        ProductDetailWidget entity = (ProductDetailWidget) this.getEntityFromRequest(request, entityId);

        Product product;
        if ( entity.getProductReference() != null ) {
            product = this.resolveProductDetail(entity.getProductReference()).getProductDetail();
        }
        else {
            product = (Product) request.getAttribute(PRODUCT);
            if (product == null) {
                ECommerceResult result = this.getResult(request);
                if (result instanceof ProductDetailResult) {
                    product = ((ProductDetailResult) result).getProductDetail();
                }
            }
        }
        if (product == null) {
            throw new ContentProviderException("No product found!");
        }
        entity.setProduct(product);
        request.setAttribute("entity", entity);
        request.setAttribute("linkResolver", this.linkResolver);

        final MvcData mvcData = entity.getMvcData();
        return resolveView(mvcData, "Entity", request);
    }

    /**
     * Handle product detail for an E-Commerce ECL item.
     * @param request
     * @param entityId
     * @return view
     * @throws ContentProviderException
     */
    @RequestMapping(method = RequestMethod.GET, value = "ProductDetailEclItem/{entityId}")
    public String handleProductDetailEclItem(HttpServletRequest request, @PathVariable String entityId) throws ContentProviderException {

        ECommerceEclItem entity = (ECommerceEclItem) this.getEntityFromRequest(request, entityId);
        String productId = entity.getExternalId();
        ProductDetailResult result = this.detailService.getDetail(productId);

        if (result.getProductDetail() == null) {
            throw new ContentProviderException("No product found!");
        }
        request.setAttribute("entity", entity);
        request.setAttribute("product", result.getProductDetail());
        request.setAttribute("linkResolver", this.linkResolver);

        final MvcData mvcData = entity.getMvcData();
        return resolveView(mvcData, "Entity", request);
    }

    /**
     * Handle cart.
     * @param request
     * @param entityId
     * @return view
     * @throws ContentProviderException
     */
    @RequestMapping(method = RequestMethod.GET, value = "Cart/{entityId}")
    public String handleCart(HttpServletRequest request, @PathVariable String entityId) throws ContentProviderException {

        CartWidget entity = (CartWidget) this.getEntityFromRequest(request, entityId);
        entity.setCart(this.getCart(request));
        request.setAttribute("entity", entity);
        request.setAttribute("linkResolver", this.linkResolver);

        final MvcData mvcData = entity.getMvcData();
        return resolveView(mvcData, "Entity", request);
    }

    /**
     * Get the entity from the request
     * @param request
     * @param entityId
     * @return
     */
    protected EntityModel getEntityFromRequest(HttpServletRequest request, String entityId) {
        final EntityModel entity = (EntityModel) request.getAttribute("_entity_");
        if (entity == null) {
            LOG.error("Entity not found in request: {}", entityId);
            throw new NotFoundException("Entity not found in request: " + entityId);
        }
        return entity;
    }

    /**
     * Get stored cart from the HTTP session.
     *
     * @param request
     * @return cart
     */
    private Cart getCart(HttpServletRequest request) {
        return (Cart) request.getSession().getAttribute(Cart.CART_URI.toString());
    }


    /**
     * Get current query result from the HTTP request.
     * @param request
     * @return result
     */
    protected QueryResult getQueryResult(HttpServletRequest request) {
       return (QueryResult) this.getResult(request);
    }

    /**
     * Get current E-Commerce result from the HTTP request.
     * @param request
     * @return result
     */
    protected ECommerceResult getResult(HttpServletRequest request) {
        ECommerceResult result = (ECommerceResult) request.getAttribute(RESULT);
        if ( result == null ) {
            // Fallback to getting result from the page template (needed when in XPM mode)
            //
            result = getResultFromPageTemplate(request);
        }
        return result;
    }

    /**
     * Resolve category model from a CMS category reference.
     * @param categoryReference
     * @return category
     */
    protected Category resolveCategoryModel(ECommerceCategoryReference categoryReference) {
        Category category = null;
        if ( categoryReference.getCategoryPath() != null ) {
            category = this.categoryService.getCategoryByPath(categoryReference.getCategoryPath());
        }
        else if ( categoryReference.getCategoryRef() != null ) {
            category = this.categoryService.getCategoryById(categoryReference.getCategoryRef().getExternalId());
        }
        else if ( categoryReference.getCategoryId() != null ) {
            category = this.categoryService.getCategoryById(categoryReference.getCategoryId());
        }
        if ( category != null ) {
            categoryReference.setCategory(category);
        }
        return category;
    }

    /**
     * Resolve product detail model from a CMS product reference.
     * @param productReference
     * @return product detail
     * @throws ContentProviderException
     */
    protected ProductDetailResult resolveProductDetail(ECommerceProductReference productReference) throws ContentProviderException {
        String productId;
        if ( productReference.getProductRef() != null ) {
            productId = productReference.getProductRef().getExternalId();
        }
        else if ( productReference.getProductId() != null ) {
            productId = productReference.getProductId();
        }
        else {
            throw new ContentProviderException("Invalid E-Commerce Product Reference!");
        }
        ProductDetailResult result = this.detailService.getDetail(productId);
        return result;
    }

    /**
     * Get E-Commerce result from a page template (is used when doing inline editing of E-Commerce driven pages).
     * @param request
     * @return result
     */
    protected ECommerceResult getResultFromPageTemplate(HttpServletRequest request) {

        // TODO: Use thread local here to optimize the search to avoid duplicate calls...
        //

        // TODO: Take localization in consideration here

        String requestPath = webRequestContext.getRequestPath();
        if ( requestPath.startsWith(webRequestContext.getLocalization().localizePath("/categories")) ) {
            final Category category = this.getCategoryFromPageTemplate(requestPath);
            Query query = this.queryService.newQuery();
            query.category(category);
            query.facets(this.getFacets(request));
            this.getQueryContributions(request, query);

            return this.queryService.query(query);
        }
        else if ( requestPath.startsWith(webRequestContext.getLocalization().localizePath("/products")) ) {
            String productId = requestPath.replaceFirst(webRequestContext.getLocalization().localizePath("/products/"), "").replace(".html", "");
            return this.detailService.getDetail(productId);
        }
        return null;
    }

    /**
     * Get query contributions from the different E-Commerce widgets on the page.
     * @param request
     * @param query
     */
    protected void getQueryContributions(HttpServletRequest request, Query query) {

        PageModel templatePage = (PageModel) request.getAttribute(PAGE_MODEL);
        for ( RegionModel region : templatePage.getRegions() ) {
            for (EntityModel entity : region.getEntities() ) {
                if ( entity instanceof QueryInputContributor) {
                    ((QueryInputContributor) entity).contributeToQuery(query);
                }
            }
        }
    }

    /**
     * Extracts the category from the page template path.
     * @param requestPath
     * @return  category
     */
    protected Category getCategoryFromPageTemplate(String requestPath) {
        // Try to get query result based on the page url cat1-cat2-cat3
        //
        final String categoryPath = requestPath.replaceFirst(webRequestContext.getLocalization().localizePath("/categories/"), "").replace(".html", "").replace("-", "/");
        Category category = this.categoryService.getCategoryByPath(categoryPath);
        if ( category == null ) {
            // Try with category ID
            //
            category = this.categoryService.getCategoryById(categoryPath.replaceAll("/", ""));
        }
        return category;
    }

    /**
     * Get category stored on the HTTP request.
     * @param request
     * @return category
     */
    protected Category getCategory(HttpServletRequest request) {
        Category category = (Category) request.getAttribute(CATEGORY);
        if ( category == null ) {
            // Fallback to get the category from the page template (needed when in XPM mode)
            //
            category = getCategoryFromPageTemplate(webRequestContext.getRequestPath());
        }
        return category;
    }

    protected void getPromotions(PromotionsWidget entity, HttpServletRequest request) throws ContentProviderException {
        ECommerceResult result;
        if ( entity.getCategoryReference() != null ) {
            Category category = this.resolveCategoryModel(entity.getCategoryReference());
            Query query = this.queryService.newQuery();
            query.category(category);
            if ( entity.getViewType() != null ) {
                query.viewType(ViewType.valueOf(entity.getViewType().toUpperCase()));
            }
            result = this.queryService.query(query);
        }
        else if ( entity.getProductReference() != null ) {
            result = this.resolveProductDetail(entity.getProductReference());
        }
        else {
            result = this.getResult(request);
        }
        if ( result != null ) {
            entity.setPromotions(result.getPromotions());
        }

    }

    /**
     * Get current URL prefix (category or search result page).
     * @param request
     * @return prefix
     */
    protected String getUrlPrefix(HttpServletRequest request) {
        String urlPrefix = (String) request.getAttribute(URL_PREFIX);
        if ( urlPrefix == null ) {
            urlPrefix = "/c"; // fallback to default category URL prefix
        }
        return urlPrefix;
    }

    /**
     * Get root category title (used in breadcrumbs etc).
     * @param request
     * @return title
     */
    protected String getRootCategoryTitle(HttpServletRequest request) {
        String rootCategoryTitle= (String) request.getAttribute(ROOT_CATEGORY_TITLE);
        /*if ( rootCategoryTitle == null ) {
            rootCategoryTitle = "Store"; // fallback to default
        } */
        return rootCategoryTitle;
    }

    /**
     * Get facets from the HTTP request.
     * @param request
     * @return facets
     */
    protected List<FacetParameter> getFacets(HttpServletRequest request) {
        List<FacetParameter> facets = (List<FacetParameter>) request.getAttribute(FACETS);
        return facets;
    }

    /**
     * Get start index from the HTTP request.
     * @param request
     * @return index
     */
    protected int getStartIndex(HttpServletRequest request) {
        String startIndex = request.getParameter("startIndex");
        if ( startIndex != null ) {
            return Integer.parseInt(startIndex);
        }
        return 0;
    }

    /**
     * Check if current user is in a XPM session.
     * @param request
     * @return true if in session preview, otherwise false
     */
    protected boolean isSessionPreview(HttpServletRequest request) {

        if ( request != null ) {
            Boolean inXpmSession = (Boolean) request.getSession().getAttribute(ECommerceSessionAttributes.IN_XPM_SESSION);
            if ( inXpmSession != null ) {
                return inXpmSession;
            }
        }
        return false;
    }

    /**
     * Process navigation links in product listers (next, previous etc).
     * @param lister
     * @param result
     * @param facets
     */
    protected void processListerNavigationLinks(ProductListerWidget lister, QueryResult result, List<FacetParameter> facets) {

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

    /**
     * Get facet link based on the provided set of facet parameters.
     * @param facets
     * @return link
     */
    protected String getFacetLink(List<FacetParameter> facets) {

        // TODO: Use the link resolver here!!!!!

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

    /**
     * Build in-context edit controls.
     * @param request
     */
    protected void buildInContextControls(HttpServletRequest request) {

        QueryResult queryResult = this.getQueryResult(request);
        if ( queryResult != null && this.editService != null ) {
            EditMenu editMenu = this.editService.getInContextMenuItems(queryResult.getQuery());
            request.setAttribute("editMenu", editMenu);
        }

    }

    @Override
    protected ViewModel enrichModel(ViewModel model, HttpServletRequest httpServletRequest) throws Exception {
        try {
            if (model instanceof PromotionsWidget) {

                PromotionsWidget promotionsWidget = (PromotionsWidget) model;
                this.getPromotions(promotionsWidget, httpServletRequest);

                // Simplify the result so it can be serialized
                //
                Category category = promotionsWidget.getCategoryReference().getCategory();
                if ( category != null ) {
                    category = new SimpleCategory(category);
                }
                promotionsWidget.getCategoryReference().setCategory(category);

                if ( promotionsWidget.getPromotions() != null ) {
                    ArrayList<Promotion> promotions = new ArrayList<>();
                    for (Promotion promotion : promotionsWidget.getPromotions()) {
                        if ( promotion instanceof ProductsPromotion ) {
                            promotions.add(new SimpleProductsPromotion((ProductsPromotion) promotion));
                        }
                        else {
                            promotions.add(promotion);
                        }
                    }
                    promotionsWidget.setPromotions(promotions);
                }
            }
        }
        catch ( ContentProviderException e ) {
            LOG.error("Could not enrich model.", e);
        }
        return model;
    }
    
}
