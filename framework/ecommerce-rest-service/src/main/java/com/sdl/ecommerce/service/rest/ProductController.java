package com.sdl.ecommerce.service.rest;

import com.sdl.ecommerce.api.*;
import com.sdl.ecommerce.api.model.Category;
import com.sdl.ecommerce.api.model.FacetParameter;
import com.sdl.ecommerce.api.model.Product;
import com.sdl.ecommerce.service.model.ErrorMessage;
import com.sdl.ecommerce.service.model.RestProduct;
import com.sdl.ecommerce.service.model.RestQueryResult;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * Product Controller
 *
 * @author nic
 */
@RestController
@RequestMapping("/ecommerce.svc/rest/v1/product")
@Slf4j
public class ProductController {

    private static final Logger LOG = LoggerFactory.getLogger(ProductController.class);

    @Autowired
    private ProductCategoryService categoryService;

    @Autowired
    private ProductQueryService productQueryService;

    @Autowired
    private ProductDetailService productDetailService;

    @Autowired
    private LocalizationService localizationService;

    @RequestMapping("/query")
    public ResponseEntity query(@RequestParam(required = false) String categoryId,
                             @RequestParam(required = false) String categoryIds,
                             @RequestParam(required = false) String searchPhrase,
                             @RequestParam(required = false) String facets,
                             @RequestParam(required = false) Integer startIndex,
                             @RequestParam(required = false) Integer viewSize,
                             @RequestParam(required = false) String viewType,
                             @RequestParam(required = false) String contextData) throws ECommerceException {

        // Build E-Commerce query based on input query parameters
        //
        Query query = this.productQueryService.newQuery();
        if ( categoryId != null ) {
            Category category = this.categoryService.getCategoryById(categoryId);
            if (category == null) {
                LOG.error("Could not find category with ID: " + categoryId);
                return ResponseEntity
                        .status(HttpStatus.NOT_FOUND)
                        .body(new ErrorMessage("Could not find category with ID: " + categoryId));
            }
            query.category(category);
        } else if (categoryIds != null) {
            List<Category> categories = new ArrayList<>();
            StringTokenizer tokenizer = new StringTokenizer(categoryIds, "|");
            while (tokenizer.hasMoreTokens()) {
                String categoryIdInList = tokenizer.nextToken();
                Category category = this.categoryService.getCategoryById(categoryIdInList);
                if (category == null) {
                    LOG.error("Could not find category with ID: " + categoryIdInList);
                    return ResponseEntity
                            .status(HttpStatus.NOT_FOUND)
                            .body(new ErrorMessage("Could not find category with ID: " + categoryIdInList));
                }
                categories.add(category);
            }
            query.categories(categories);
        }
        if ( searchPhrase != null ) {
            query.searchPhrase(searchPhrase);
        }
        if ( facets != null ) {
            StringTokenizer tokenizer = new StringTokenizer(facets, "=&");
            while ( tokenizer.hasMoreTokens() ) {
                String facetName = tokenizer.nextToken();
                String facetValues = tokenizer.nextToken();
                FacetParameter facet = new FacetParameter(facetName, facetValues);
                query.facet(facet);
            }
        }
        if ( startIndex != null ) {
            query.startIndex(startIndex);
        }
        if ( viewSize != null ) {
            query.viewSize(viewSize);
        }
        if ( viewType != null ) {
            query.viewType(ViewType.valueOf(viewType));
        }
        if (contextData != null) {
            StringTokenizer tokenizer = new StringTokenizer(contextData, "=&");
            while ( tokenizer.hasMoreTokens() ) {
                String name = tokenizer.nextToken();
                String value = tokenizer.nextToken();
                query.contextData(name, value);
            }
        }

        // Consume system facets which will be handled as hidden facets in the query and will not be shown for the end-user
        //
        String systemFacets = this.localizationService.getLocalizedConfigProperty("system-facets");
        if ( systemFacets != null ) {
            StringTokenizer tokenizer = new StringTokenizer(systemFacets, "=&");
            while ( tokenizer.hasMoreTokens() ) {
                String facetName = tokenizer.nextToken();
                String facetValues = tokenizer.nextToken();
                FacetParameter facet = new FacetParameter(facetName + "_hidden", facetValues);
                query.facet(facet);
            }
        }

        QueryResult queryResult = this.productQueryService.query(query);
        return new ResponseEntity<>(new RestQueryResult(queryResult), HttpStatus.OK);
    }

    @RequestMapping("/{productId}")
    public ResponseEntity get(@PathVariable String productId, @RequestParam Map<String,String> variantAttributes) throws ECommerceException {

        ProductDetailResult result;
        if (variantAttributes != null && variantAttributes.size() > 0) {
           result = this.productDetailService.getDetail(productId, variantAttributes);
        }
        else {
            result = this.productDetailService.getDetail(productId);
        }

        if ( result.getProductDetail() == null ) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorMessage("Product with ID '" + productId + "' was not found"));
        }
        return new ResponseEntity<Product>(new RestProduct(result), HttpStatus.OK);
    }

    @ExceptionHandler({ ECommerceException.class, Exception.class })
    public final ResponseEntity<ErrorMessage> handleAllExceptions(Exception ex) {
        LOG.error("Exception when querying product(s)", ex);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorMessage(ex.getMessage()));
    }

}
