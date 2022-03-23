package com.sdl.ecommerce.dxa.model;

import com.sdl.ecommerce.api.model.Product;
import com.sdl.ecommerce.api.model.ProductsPromotion;

import java.util.ArrayList;
import java.util.List;

/**
 * SimpleProductsPromotion
 *
 * @author nic
 */
public class SimpleProductsPromotion implements ProductsPromotion {

    private ProductsPromotion productsPromotion;

    public SimpleProductsPromotion(ProductsPromotion productsPromotion) {
        this.productsPromotion = productsPromotion;
    }

    @Override
    public String getId() {
        return this.productsPromotion.getId();
    }

    @Override
    public List<Product> getProducts() {
        List<Product> products = new ArrayList<>();
        for ( Product product : this.productsPromotion.getProducts() ) {
            products.add(new SimpleProduct(product));
        }
        return products;
    }

    @Override
    public String getName() {
        return this.productsPromotion.getName();
    }

    @Override
    public String getEditUrl() {
        return null;
    }

    @Override
    public String getTitle() {
        return this.productsPromotion.getTitle();
    }
}
