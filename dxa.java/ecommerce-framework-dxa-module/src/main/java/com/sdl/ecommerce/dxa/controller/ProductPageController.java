package com.sdl.ecommerce.dxa.controller;

import com.sdl.ecommerce.api.ProductDetailResult;
import com.sdl.ecommerce.api.ProductDetailService;
import static com.sdl.ecommerce.dxa.ECommerceRequestAttributes.*;

import com.sdl.ecommerce.api.model.Category;
import com.sdl.ecommerce.dxa.model.SimpleProduct;
import com.sdl.webapp.common.api.content.ContentProviderException;
import com.sdl.webapp.common.api.content.PageNotFoundException;
import com.sdl.webapp.common.api.localization.Localization;
import com.sdl.webapp.common.api.model.MvcData;
import com.sdl.webapp.common.api.model.PageModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Product Page Controller
 * Manage product detail pages. It provides E-Commerce data for current product (extracted from the URL) which the different
 * widgets can consume.
 * Provides the following SEO friendly URL format: /p/[product name]/[product ID/SKU]. Example: /p/denim-blue-skirt/49392823
 *
 * @author nic
 */
@Controller
@RequestMapping("/p")
public class ProductPageController extends AbstractECommercePageController {

    private static final Logger LOG = LoggerFactory.getLogger(ProductPageController.class);

    @Autowired
    private ProductDetailService detailService;

    /**
     * Handle product detail page.
     * @param request
     * @param response
     * @return view
     * @throws ContentProviderException
     */
    @RequestMapping(method = RequestMethod.GET, value = "/**", produces = {MediaType.TEXT_HTML_VALUE})
    public String handleProductDetailPage(HttpServletRequest request, HttpServletResponse response) throws ContentProviderException {

        final String requestPath = request.getRequestURI().replaceFirst("/p", "");
        final Localization localization = webRequestContext.getLocalization();
        final String[] pathTokens = requestPath.split("/");
        final String productSeoId;
        String productId;
        if ( pathTokens.length == 3 ) {
            productSeoId = pathTokens[1];
            productId = pathTokens[2];
        }
        else if ( pathTokens.length == 2 ) {
            productSeoId = null;
            productId = pathTokens[1];
        }
        else {
            throw new PageNotFoundException("Invalid product detail URL.");
        }

        // Handle some special characters
        //
        productId = productId.replace("__plus__", "+");

        // Get variant attributes
        //
        Map<String,String> variantAttributes = new HashMap<>();
        Map<String, String[]> requestParameters = request.getParameterMap();
        for ( String parameterName : requestParameters.keySet() ) {
            variantAttributes.put(parameterName, requestParameters.get(parameterName)[0]);
        }

        ProductDetailResult detailResult = null;

        if ( variantAttributes.size() > 0 ) {
            detailResult = this.detailService.getDetail(productId, variantAttributes);
        }

        if ( detailResult == null || detailResult.getProductDetail() == null ) {
            // Fallback on the product ID without variant attributes
            //
            detailResult = this.detailService.getDetail(productId);
        }
        if ( detailResult != null && detailResult.getProductDetail() != null ) {

            request.setAttribute(PRODUCT_ID, productId);
            request.setAttribute(PRODUCT, detailResult.getProductDetail());
            request.setAttribute(RESULT, detailResult);
            request.setAttribute(URL_PREFIX, localization.localizePath("/c"));
            final PageModel templatePage = resolveTemplatePage(request, this.getSearchPath(localization, productSeoId, productId, detailResult.getProductDetail().getCategories()));
            templatePage.setTitle(detailResult.getProductDetail().getName());

            final MvcData mvcData = templatePage.getMvcData();
            return this.viewResolver.resolveView(mvcData, "Page", request);
        }
        throw new PageNotFoundException("Product detail page not found.");
    }

    @Override
    protected Object getDataToBeFormatted(HttpServletRequest request) throws ContentProviderException {

        // TODO: Refactor into one method handling retrieval of the product detail

        final String requestPath = request.getRequestURI().replaceFirst("/p", "");
        final Localization localization = webRequestContext.getLocalization();
        final String[] pathTokens = requestPath.split("/");
        final String productSeoId;
        String productId;
        if ( pathTokens.length == 3 ) {
            productSeoId = pathTokens[1];
            productId = pathTokens[2];
        }
        else if ( pathTokens.length == 2 ) {
            productSeoId = null;
            productId = pathTokens[1];
        }
        else {
            throw new PageNotFoundException("Invalid product detail URL.");
        }

        // Handle some special characters
        //
        productId = productId.replace("__plus__", "+");

        // Get variant attributes
        //
        Map<String,String> variantAttributes = new HashMap<>();
        Map<String, String[]> requestParameters = request.getParameterMap();
        for ( String parameterName : requestParameters.keySet() ) {
            variantAttributes.put(parameterName, requestParameters.get(parameterName)[0]);
        }

        ProductDetailResult detailResult = this.detailService.getDetail(productId, variantAttributes);
        if ( detailResult.getProductDetail() == null ) {
            LOG.warn("Could not find product variant page. Falling back on the main product page");

            // Fallback on the product ID without variant attributes
            //
            detailResult = this.detailService.getDetail(productId);
        }
        if ( detailResult != null && detailResult.getProductDetail() != null ) {
            return new SimpleProduct(detailResult.getProductDetail());
        }
        return null;
    }

    /**
     * Get search path for finding appropriate template page.
     * @param localization
     * @param productSeoId
     * @param productId
     * @return search path
     */
    protected List<String> getSearchPath(Localization localization, String productSeoId, String productId, List<Category> categories) {

        // Should we allow some alternative lookup mechanism here as well? Such as search for

        List<String> searchPath = new ArrayList<>();
        String basePath = localization.localizePath("/products/");
        if ( productSeoId != null ) {
            searchPath.add(basePath + productSeoId);

        }
        searchPath.add(basePath + productId);
        if ( categories != null ) {
            for ( Category category : categories ) {
                searchPath.add(basePath + category.getId());
            }
        }
        searchPath.add(basePath + "generic");
        return searchPath;
    }
}