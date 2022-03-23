package com.sdl.ecommerce.dxa.controller;

import com.sdl.ecommerce.api.*;
import static com.sdl.ecommerce.dxa.ECommerceRequestAttributes.*;

import com.sdl.ecommerce.api.model.Category;
import com.sdl.ecommerce.api.model.FacetParameter;
import com.sdl.ecommerce.dxa.model.SimpleQueryResult;
import com.sdl.webapp.common.api.content.ContentProviderException;
import com.sdl.webapp.common.api.content.PageNotFoundException;
import com.sdl.webapp.common.api.formats.DataFormatter;
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
import java.util.List;
import java.util.StringTokenizer;

/**
 * Category Page Controller
 * Manage all category pages. All data comes from the E-Commerce system, while the actual look&feel is maintained by a set of overridable template pages.
 * Provides the following SEO friendly URL format for categories: /c/[category1]/[sub-category2/?[facet]&[facet2]
 *
 * @author nic
 */
@Controller
@RequestMapping("/c")
public class CategoryPageController extends AbstractECommercePageController {

    private static final Logger LOG = LoggerFactory.getLogger(CategoryPageController.class);

    static final String STORE_TITLE = "Store"; // TODO: Fetch this from the breadcrumb info instead!!!

    @Autowired
    private DataFormatter dataFormatters;

    /**
     * Handle category page.
     * @param request
     * @param response
     * @return view
     * @throws ContentProviderException
     */
    @RequestMapping(method = RequestMethod.GET, value = "/**", produces = {MediaType.TEXT_HTML_VALUE})
    public String handleCategoryPage(HttpServletRequest request, HttpServletResponse response) throws ContentProviderException {

        // TODO: Have the '/c' configurable
        //
        final String requestPath = request.getRequestURI().replaceFirst("/c", "");
        final Localization localization = webRequestContext.getLocalization();
        final Category category = this.categoryService.getCategoryByPath(requestPath);
        final List<FacetParameter> facets = this.getFacetParametersFromRequestMap(request.getParameterMap());
        final PageModel templatePage = resolveTemplatePage(request, this.getSearchPath(localization, requestPath, category));

        final Query query = this.queryService.newQuery();
        this.getQueryContributions(templatePage, query);

        final QueryResult result = this.queryService.query(query.
                category(category).
                facets(facets).
                startIndex(this.getStartIndex(request)));

        if ( result != null ) {

            if ( result.getRedirectLocation() != null ) {
                return "redirect:" + this.linkResolver.getLocationLink(result.getRedirectLocation());
            }

            request.setAttribute(CATEGORY, category);
            request.setAttribute(RESULT, result);
            request.setAttribute(FACETS, facets);
            request.setAttribute(URL_PREFIX, localization.localizePath("/c"));

            if ( category != null ) {
                templatePage.setTitle(category.getName());
            }
            else {
                templatePage.setTitle(STORE_TITLE); // Root category
            }

            final MvcData mvcData = templatePage.getMvcData();
            return this.viewResolver.resolveView(mvcData, "Page", request);
        }

        throw new PageNotFoundException("Category page not found.");
    }


    @Override
    protected Object getDataToBeFormatted(final HttpServletRequest request) throws ContentProviderException {

        final String requestPath = request.getRequestURI().replaceFirst("/c", "");
        final Category category = this.categoryService.getCategoryByPath(requestPath);
        final List<FacetParameter> facets = this.getFacetParametersFromRequestMap(request.getParameterMap());
        final Query query = this.queryService.newQuery();

        final QueryResult result = this.queryService.query(query.
                category(category).
                facets(facets).
                startIndex(this.getStartIndex(request)));

        if (result != null) {

            return new SimpleQueryResult(result);

        }
        return null;
    }

    /**
     * Get search path to find an appropriate CMS template page for current category.
     * @param localization
     * @param url
     * @param category
     * @return search path
     */
    protected List<String> getSearchPath(Localization localization, String url, Category category) {
        List<String> searchPath = new ArrayList<>();
        String basePath = localization.localizePath("/categories/");
        String categoryPath = basePath; // TODO: Have this configurable. Should this be placed in System instead?
        Category currentCategory = category;
        while ( currentCategory != null ) {
            searchPath.add(categoryPath + category.getId());
            currentCategory = currentCategory.getParent();
        }
        StringTokenizer tokenizer = new StringTokenizer(url, "/");
        while ( tokenizer.hasMoreTokens() ) {
            categoryPath += tokenizer.nextToken();
            searchPath.add(0, categoryPath);
            if ( tokenizer.hasMoreTokens() ) {
                categoryPath += "-";
            }
        }
        searchPath.add(basePath + "generic");
        return searchPath;
    }


    // TODO: Exceptionhandler is needed here

}

