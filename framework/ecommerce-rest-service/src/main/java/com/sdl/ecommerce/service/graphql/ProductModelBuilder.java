package com.sdl.ecommerce.service.graphql;

import com.merapar.graphql.GraphQlFields;
import com.sdl.ecommerce.service.model.RestProduct;
import com.sdl.ecommerce.service.model.RestQueryResult;
import com.sdl.ecommerce.service.model.RestVariantAttribute;
import graphql.annotations.GraphQLAnnotations;
import graphql.annotations.GraphQLField;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLInputObjectType;
import graphql.schema.GraphQLList;
import graphql.schema.GraphQLObjectType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.List;

import static com.merapar.graphql.base.GraphQlFieldsHelper.FILTER;
import static com.merapar.graphql.base.GraphQlFieldsHelper.getFilterMap;
import static graphql.Scalars.GraphQLInt;
import static graphql.Scalars.GraphQLString;
import static graphql.schema.GraphQLArgument.newArgument;
import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition;
import static graphql.schema.GraphQLInputObjectField.newInputObjectField;
import static graphql.schema.GraphQLInputObjectType.newInputObject;

/**
 * ProductModelBuilder
 *
 * @author nic
 */
@Component
public class ProductModelBuilder implements GraphQlFields {

    @Autowired
    private ProductDataFetcher productDataFetcher;

    private GraphQLObjectType productType;

    private GraphQLObjectType productListType;

    private GraphQLObjectType variantAttributeType;

    @PostConstruct
    public void initialize() {
        this.createTypes();
    }

    // TODO: Can we automate this in a nice way???

    @Override
    public List<GraphQLFieldDefinition> getQueryFields() {

        // TODO: Can the variant attributes be types somehow???
        GraphQLInputObjectType variantAttributeType = newInputObject().name("variantAttribute")
                .field(newInputObjectField().name("name").type(GraphQLString).build())
                .field(newInputObjectField().name("value").type(GraphQLString).build()).build();

        // TODO: How to specify the query???

        // TODO: How to map product query to a logical entity????

        GraphQLInputObjectType filterInputType = newInputObject().name("filterInput")
                .field(newInputObjectField().name("id").type(GraphQLString).build())
                .field(newInputObjectField().name("variantAttributes")
                        .type(new GraphQLList(variantAttributeType)).build()).build();

        GraphQLInputObjectType queryInputType = newInputObject().name("queryInput")
                .field(newInputObjectField().name("categoryId").type(GraphQLString).build())
                .field(newInputObjectField().name("searchPhrase").type(GraphQLString).build())
                .field(newInputObjectField().name("facets").type(GraphQLString).build())
                .field(newInputObjectField().name("startIndex").type(GraphQLInt).build())
                .field(newInputObjectField().name("viewSize").type(GraphQLInt).build())
                .field(newInputObjectField().name("viewType").type(GraphQLString).build())
                .build();

        // TODO: Add facets which has the same structure as variant attributes

        GraphQLFieldDefinition productField = newFieldDefinition()
                .name("product").description("E-Commerce Product Detail")
                .type(productType)
                .argument(newArgument().name(FILTER).type(filterInputType).build())
                .dataFetcher(environment -> this.productDataFetcher.getProductDetail(getFilterMap(environment)))
                .build();

        GraphQLFieldDefinition productsField = newFieldDefinition()
                .name("productQuery").description("E-Commerce Product Query")
                .type(productListType)
                .argument(newArgument().name(FILTER).type(queryInputType).build())
                .dataFetcher(environment -> this.productDataFetcher.query(getFilterMap(environment)))
                .build();

        return Arrays.asList(productField, productsField);
    }

    @Override
    public List<GraphQLFieldDefinition> getMutationFields() {
        return null;
    }

    private void createTypes() {

        variantAttributeType = GraphQLAnnotations.object(RestVariantAttribute.class);

        productType = GraphQLAnnotations.object(RestProduct.class);

        productListType = GraphQLAnnotations.object(RestQueryResult.class);

        /*
        productType = newObject().name("product").description("E-Commerce Product")
                .field(newFieldDefinition().name("id").description("Product ID").type(GraphQLString).build())
                .field(newFieldDefinition().name("name").description("Name").type(GraphQLString).build())
                .field(newFieldDefinition().name("description").description("Description").type(GraphQLString).build())
                .field(newFieldDefinition().name("thumbnailUrl").description("Thumbnail URL").type(GraphQLString).build())
                // TODO: Add more fields here!!
                .build();
         */
    }
}
