package com.sdl.ecommerce.service.graphql;

import com.merapar.graphql.base.TypedValueMap;
import com.sdl.ecommerce.api.*;
import com.sdl.ecommerce.api.model.Category;
import com.sdl.ecommerce.service.model.RestProduct;
import com.sdl.ecommerce.service.model.RestQueryResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * ProductDataFetcher
 *
 * @author nic
 */
@Component
public class ProductDataFetcher {

    @Autowired
    private ProductCategoryService categoryService;

    @Autowired
    private ProductQueryService productQueryService;

    @Autowired
    private ProductDetailService productDetailService;

    public RestProduct getProductDetail(TypedValueMap arguments) {
        String productId = arguments.get("id");

        // TODO: Add support for variant attributes here as well!!

        if ( productId != null ) {
            ProductDetailResult result = this.productDetailService.getDetail(productId);
            if ( result != null ) {
                return new RestProduct(result.getProductDetail());
            }
        }

        // TODO: Throw an exception here???
        return null;
    }

    public RestQueryResult query(TypedValueMap arguments) {

        Query query = this.productQueryService.newQuery();
        String categoryId = arguments.get("categoryId");
        String searchPhrase = arguments.get("searchPhrase");
        Integer startIndex = arguments.get("startIndex");
        Integer viewSize = arguments.get("viewSize");
        String viewType = arguments.get("viewType");
        if ( categoryId != null ) {
            Category category = this.categoryService.getCategoryById(categoryId);
            query.category(category);
        }
        if ( searchPhrase != null ) {
            query.searchPhrase(searchPhrase);
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

        QueryResult result = this.productQueryService.query(query);
        if ( result != null ) {
            return new RestQueryResult(result);
        }
        return null;

    }
}
