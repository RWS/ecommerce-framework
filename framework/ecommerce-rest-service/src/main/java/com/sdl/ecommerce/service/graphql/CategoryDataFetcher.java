package com.sdl.ecommerce.service.graphql;

import com.merapar.graphql.base.TypedValueMap;
import com.sdl.ecommerce.api.ProductCategoryService;
import com.sdl.ecommerce.api.model.Category;
import com.sdl.ecommerce.service.model.RestCategory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * CategoryDataFetcher
 *
 * @author nic
 */
@Component
public class CategoryDataFetcher {

    @Autowired
    private ProductCategoryService categoryService;

    public RestCategory getCategory(TypedValueMap arguments) {
        String categoryId = arguments.get("id");
        String path = arguments.get("path");

        // TODO: Handle exceptions here

        if ( categoryId != null ) {
            Category category = this.categoryService.getCategoryById(categoryId);
            if ( category != null ) {
                return new RestCategory(category);
            }
        }
        else if ( path != null ) {
            Category category = this.categoryService.getCategoryByPath(path);
            if ( category != null ) {
                return new RestCategory(category);
            }
        }
        else {
            // Get top-level categories
            //
            List<Category> topLevelCategories = this.categoryService.getTopLevelCategories();
            return new RestCategory(null, topLevelCategories);
        }
        return null;
    }

    public List<RestCategory> getCategories(TypedValueMap arguments) {
        String categoryId = arguments.get("id");
        String path = arguments.get("path");

        // TODO: Handle exceptions here
        if ( categoryId != null ) {
            Category category = this.categoryService.getCategoryById(categoryId);
            return toGraphQL(category);
        }
        else if ( path != null ) {
            Category category = this.categoryService.getCategoryByPath(path);
            return toGraphQL(category);
        }
        else {
            // Get top-level categories
            //
            List<Category> topLevelCategories = this.categoryService.getTopLevelCategories();
            return toGraphQL(topLevelCategories);
        }
    }

    private List<RestCategory> toGraphQL(Category category) {
        if ( category == null ) {
            return Collections.emptyList();
        }
        return toGraphQL(category.getCategories());
    }

    private List<RestCategory> toGraphQL(List<Category> categories) {
        List<RestCategory> responseList = new ArrayList<>();
        categories.forEach(cat ->  responseList.add(new RestCategory(cat)));
        return responseList;
    }
}


