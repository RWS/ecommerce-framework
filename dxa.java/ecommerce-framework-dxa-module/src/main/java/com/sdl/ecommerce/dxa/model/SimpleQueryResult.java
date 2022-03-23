package com.sdl.ecommerce.dxa.model;

import com.sdl.ecommerce.api.QueryResult;
import com.sdl.ecommerce.api.model.Category;
import com.sdl.ecommerce.api.model.FacetGroup;
import com.sdl.ecommerce.api.model.Product;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Simple Query Result
 *
 * @author nic
 */
public class SimpleQueryResult {

    private QueryResult result;

    public SimpleQueryResult(QueryResult result) {
        this.result = result;
    }

    public int getTotalCount() {
        return this.result.getTotalCount();
    }

    public int getStartIndex() {
        return this.result.getStartIndex();
    }

    public int getCurrentSet() {
        return this.result.getCurrentSet();
    }

    public SimpleCategory getCategory() {
        return new SimpleCategory(this.result.getCategory());
    }

    public List<FacetGroup> getFacetGroups() {
        return this.result.getFacetGroups();
    }

    public List<Product> getProducts() {
        //return this.result.getProducts().forEach(product -> new SimpleProduct(product));
        ArrayList<Product> products = new ArrayList<>();
        for ( Product product : this.result.getProducts() ) {
            products.add(new SimpleProduct(product));
        }
        return products;
    }

}
