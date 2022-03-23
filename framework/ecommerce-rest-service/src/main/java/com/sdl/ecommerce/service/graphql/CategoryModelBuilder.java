package com.sdl.ecommerce.service.graphql;

import com.merapar.graphql.GraphQlFields;
import com.sdl.ecommerce.service.graphql.CategoryDataFetcher;
import com.sdl.ecommerce.service.model.RestCategory;
import graphql.annotations.GraphQLAnnotations;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLInputObjectType;
import graphql.schema.GraphQLObjectType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.List;

import static com.merapar.graphql.base.GraphQlFieldsHelper.FILTER;
import static com.merapar.graphql.base.GraphQlFieldsHelper.getFilterMap;
import static graphql.Scalars.GraphQLString;
import static graphql.schema.GraphQLArgument.newArgument;
import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition;
import static graphql.schema.GraphQLInputObjectField.newInputObjectField;
import static graphql.schema.GraphQLInputObjectType.newInputObject;

/**
 * Category Model Builder
 *
 * @author nic
 */
@Component
public class CategoryModelBuilder implements GraphQlFields {

    @Autowired
    private CategoryDataFetcher categoryDataFetcher;

    private GraphQLObjectType categoryType;

    @PostConstruct
    public void initialize() {
        this.createTypes();
    }

    @Override
    public List<GraphQLFieldDefinition> getQueryFields() {

        GraphQLInputObjectType filterInputType = newInputObject().name("filterInput")
                .field(newInputObjectField().name("id").type(GraphQLString).build())
                .field(newInputObjectField().name("path").type(GraphQLString)).build();

        GraphQLFieldDefinition categoryField = newFieldDefinition()
                .name("category").description("E-Commerce Product Category")
                .type(categoryType)
                .argument(newArgument().name(FILTER).type(filterInputType).build())
                .dataFetcher(environment -> this.categoryDataFetcher.getCategory(getFilterMap(environment)))
                .build();

        /*
        GraphQLFieldDefinition categoriesField = newFieldDefinition()
                .name("categories").description("E-Commerce Product Categories")
                .type(new GraphQLList(categoryType))
                .argument(newArgument().name(FILTER).type(filterInputType).build())
                .dataFetcher(environment -> this.categoryDataFetcher.getCategories(getFilterMap(environment)))
                .build();
                */

        return Arrays.asList(categoryField);
    }

    @Override
    public List<GraphQLFieldDefinition> getMutationFields() {
        return null;
    }

    private void createTypes() {
        this.categoryType = GraphQLAnnotations.object(RestCategory.class);
    }
}
