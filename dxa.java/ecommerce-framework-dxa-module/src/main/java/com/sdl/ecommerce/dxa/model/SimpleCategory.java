package com.sdl.ecommerce.dxa.model;

import com.sdl.ecommerce.api.model.Category;

import java.util.List;

/**
 * SimpleCategory
 *
 * @author nic
 */
public class SimpleCategory implements Category {

    private Category category;
    public SimpleCategory(Category category) {
        this.category = category;
    }

    public String getId() {
        return this.category.getId();
    }

    public String getName() {
        return this.category.getName();
    }

    @Override
    public String getPathName() {
        return this.category.getPathName();
    }

    @Override
    public Category getParent() {
        return null;
    }

    @Override
    public List<Category> getCategories() {
        return null;
    }
}
