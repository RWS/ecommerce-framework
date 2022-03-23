package com.sdl.ecommerce.service.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.sdl.ecommerce.api.model.Category;
import graphql.annotations.GraphQLDescription;
import graphql.annotations.GraphQLField;
import graphql.annotations.GraphQLName;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * REST Category
 *
 * @author nic
 */
@GraphQLDescription("E-Commerce Category")
@GraphQLName("Category")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RestCategory implements Category {

    @GraphQLField
    protected String id;

    @GraphQLField
    protected String name;

    @GraphQLField
    private String pathName;

    private RestCategory parent = null;

    @GraphQLField
    private List<RestCategory> categories = new ArrayList<>();

    @GraphQLField
    private List<String> parentIds = null;

    /**
     * Constructor
     * @param category
     */
    public RestCategory(Category category) {
        this.id = category.getId();
        this.name = category.getName();
        this.pathName = category.getPathName();

        this.parentIds = new ArrayList<>();
        Category parent = category.getParent();
        while ( parent != null ) {
            this.parentIds.add(0, parent.getId());
            parent = parent.getParent();
        }
        if ( category.getCategories() != null ) {
            category.getCategories().forEach(cat -> categories.add(new RestCategory(cat, this)));
        }
    }

    public RestCategory(Category category, List<Category> subCategories) {
        if ( category != null ) {
            this.id = category.getId();
            this.name = category.getName();
            this.pathName = category.getPathName();

            this.parentIds = new ArrayList<>();
            Category parent = category.getParent();
            while (parent != null) {
                this.parentIds.add(0, parent.getId());
                parent = parent.getParent();
            }
        }
        if ( subCategories != null ) {
            subCategories.forEach(cat -> categories.add(new RestCategory(cat, this)));
        }
    }

    /**
     * Internal constructor used for sub-ordinated categories
     * @param category
     * @param parent
     */
    private RestCategory(Category category, Category parent) {
        this.id = category.getId();
        this.name = category.getName();
        this.pathName = category.getPathName();
    }

    @Override
    public List<Category> getCategories() {
        return this.categories.stream().collect(Collectors.toList());
    }

    @Override
    public Category getParent() {
        return this.parent;
    }

    
    public List<String> getParentIds() {
        return parentIds;
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public String getName() {
        return this.name;
    }


    @Override
    public String getPathName() {
        return this.pathName;
    }

}
