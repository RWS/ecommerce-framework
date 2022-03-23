package com.sdl.ecommerce.service.graphql;

import com.merapar.graphql.GraphQlFields;
import com.sdl.ecommerce.service.model.RestCart;
import graphql.annotations.GraphQLAnnotations;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLObjectType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.List;

import static graphql.Scalars.GraphQLString;
import static graphql.schema.GraphQLArgument.newArgument;
import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition;

/**
 * CartModelBuilder
 *
 * @author nic
 */
@Component
public class CartModelBuilder implements GraphQlFields {

    @Autowired
    private CartDataFetcher cartDataFetcher;

    private GraphQLObjectType cartType;

    @PostConstruct
    public void initialize() {
        this.createTypes();
    }
    
    @Override
    public List<GraphQLFieldDefinition> getQueryFields() {
        return null;
    }

    @Override
    public List<GraphQLFieldDefinition> getMutationFields() {


        GraphQLFieldDefinition addToCart =
                newFieldDefinition()
                        .name("addToCart")
                        .description("Add E-Commerce product to cart")
                        .type(cartType)
                        .argument(newArgument()
                                .name("cartId")
                                .type(GraphQLString)
                        )
                        .argument(newArgument()
                                .name("productId")
                                .type(GraphQLString)
                        )
                        .dataFetcher(environment -> this.cartDataFetcher.addToCart(environment)).build();

        GraphQLFieldDefinition removeFromCart =
                newFieldDefinition()
                        .name("removeFromCart")
                        .description("Remove E-Commerce product from cart")
                        .type(cartType)
                        .argument(newArgument()
                                .name("cartId")
                                .type(GraphQLString)
                        )
                        .argument(newArgument()
                                .name("productId")
                                .type(GraphQLString)
                        )
                        .dataFetcher(environment -> this.cartDataFetcher.removeFromCart(environment)).build();


        return Arrays.asList(addToCart, removeFromCart);
    }

    private void createTypes() {
        this.cartType = GraphQLAnnotations.object(RestCart.class);
    }
}
