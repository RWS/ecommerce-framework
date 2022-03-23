package com.sdl.ecommerce.dxa.controller;

import com.sdl.ecommerce.api.Query;
import com.sdl.ecommerce.api.QueryResult;
import com.sdl.ecommerce.api.model.Category;
import com.sdl.ecommerce.api.model.FacetParameter;
import com.sdl.ecommerce.dxa.model.SimpleQueryResult;
import com.sdl.webapp.common.api.content.ContentProviderException;
import com.sdl.webapp.common.api.content.PageNotFoundException;
import com.sdl.webapp.common.api.localization.Localization;
import com.sdl.webapp.common.api.model.MvcData;
import com.sdl.webapp.common.api.model.PageModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.sdl.ecommerce.dxa.ECommerceRequestAttributes.*;

/**
 * Search Page Controller
 * Handles product search. Is an variation of the product category controller.
 * Provides the following SEO friendly URL format: /search/[category 1]/[sub category 2]/?[facet1]&[facet2]
 *
 * @author nic
 */
@Controller
@RequestMapping("/search")
public class SearchPageController extends AbstractECommercePageController {

    private static final Logger LOG = LoggerFactory.getLogger(SearchPageController.class);

    @RequestMapping(method = RequestMethod.GET, value = "/_redirect", produces = {MediaType.TEXT_HTML_VALUE})
    public String handleSearch(HttpServletRequest request, HttpServletResponse response) throws ContentProviderException {

        final String searchPhrases = request.getParameter("q");
        final Localization localization = this.webRequestContext.getLocalization();
        return "redirect:" + localization.localizePath("/search/") + searchPhrases;
    }

    /**
     * Handle search category page (search result with browse-able categories and facets).
     * @param request
     * @param response
     * @return view
     * @throws ContentProviderException
     * @throws IOException
     */
    @RequestMapping(method = RequestMethod.GET, value = "/**", produces = {MediaType.TEXT_HTML_VALUE})
    public String handleSearchCategoryPage(HttpServletRequest request, HttpServletResponse response) throws ContentProviderException, IOException {

        //String requestPath = webRequestContext.getRequestPath().replaceFirst("/search", "");
        final String requestPath = request.getRequestURI().replaceFirst("/search", "");
        final Localization localization = this.webRequestContext.getLocalization();

        String[] pathTokens = requestPath.split("/");
        if ( pathTokens.length < 2 ) {
            throw new PageNotFoundException("Invalid search!");
        }
        String searchPhrase = pathTokens[1];
        String categoryPath = "";
        for ( int i=2; i < pathTokens.length; i++) {
            categoryPath += "/" + pathTokens[i];
        }

        final Category category = this.categoryService.getCategoryByPath(categoryPath);
        final List<FacetParameter> facets = this.getFacetParametersFromRequestMap(request.getParameterMap());
        final PageModel templatePage = resolveTemplatePage(request, getSearchPath(localization, requestPath));

        final Query query = this.queryService.newQuery();
        this.getQueryContributions(templatePage, query);

        final QueryResult result = this.queryService.query(query.
                searchPhrase(searchPhrase).
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
            request.setAttribute(SEARCH_PHRASE, searchPhrase);
            request.setAttribute(URL_PREFIX, localization.localizePath("/search/") + searchPhrase);
            request.setAttribute(ROOT_CATEGORY_TITLE, "Search Results");

            final MvcData mvcData = templatePage.getMvcData();
            LOG.trace("Page MvcData: {}", mvcData);

            return this.viewResolver.resolveView(mvcData, "Page", request);
        }
        throw new PageNotFoundException("Search category page not found.");
    }

    @Override
    protected Object getDataToBeFormatted(HttpServletRequest request) throws ContentProviderException {
        final String requestPath = request.getRequestURI().replaceFirst("/search", "");
        final Localization localization = this.webRequestContext.getLocalization();

        String[] pathTokens = requestPath.split("/");
        if ( pathTokens.length < 2 ) {
            throw new PageNotFoundException("Invalid search!");
        }
        String searchPhrase = pathTokens[1];
        String categoryPath = "";
        for ( int i=2; i < pathTokens.length; i++) {
            categoryPath += "/" + pathTokens[i];
        }

        final Category category = this.categoryService.getCategoryByPath(categoryPath);
        final List<FacetParameter> facets = this.getFacetParametersFromRequestMap(request.getParameterMap());

        final Query query = this.queryService.newQuery();

        final QueryResult result = this.queryService.query(query.
                searchPhrase(searchPhrase).
                category(category).
                facets(facets).
                startIndex(this.getStartIndex(request)));
        if ( result != null ) {
            return new SimpleQueryResult(result);
        }
        return null;
    }

    protected List<String> getSearchPath(Localization localization, String url) {
        List<String> searchPath = new ArrayList<>();
        searchPath.add(localization.localizePath("/search-results")); // TODO: Have this configurable
        // TODO: Add possibility to override search result based on search phrases etc
        return searchPath;
    }
}

