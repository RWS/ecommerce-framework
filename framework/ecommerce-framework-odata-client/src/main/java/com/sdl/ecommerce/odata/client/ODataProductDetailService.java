package com.sdl.ecommerce.odata.client;

import com.sdl.ecommerce.api.ECommerceException;
import com.sdl.ecommerce.api.ProductDetailResult;
import com.sdl.ecommerce.api.ProductDetailService;
import com.sdl.ecommerce.api.model.impl.GenericProductDetailResult;
import com.sdl.ecommerce.odata.model.*;
import com.sdl.odata.client.BasicODataClientQuery;
import com.sdl.odata.client.FunctionImportClientQuery;
import com.sdl.odata.client.api.ODataClientQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Map;

/**
 * OData Product Detail Service
 *
 * @author nic
 */
@Component
public class ODataProductDetailService implements ProductDetailService {

    @Autowired
    private ODataClient odataClient;

    @PostConstruct
    public void initialize() {
        this.odataClient.registerModelClass(ODataProduct.class);
        this.odataClient.registerModelClass(ODataProductPrice.class);
        this.odataClient.registerModelClass(ODataProductAttribute.class);
        this.odataClient.registerModelClass(ODataProductAttributeValue.class);
        this.odataClient.registerModelClass(ODataProductVariant.class);
        this.odataClient.registerModelClass(ODataProductVariantAttributeType.class);
        this.odataClient.registerModelClass(ODataProductVariantAttributeValueType.class);
    }

    @Override
    public ProductDetailResult getDetail(String productId) throws ECommerceException {
        ODataClientQuery query = new BasicODataClientQuery.Builder()
                .withEntityType(ODataProduct.class)
                .withEntityKey("'" + productId + "'")
                .build();


        ODataProduct product = (ODataProduct) this.odataClient.getEntity(query);
        if ( product != null ) {
            return new GenericProductDetailResult(product, product.getBreadcrumbs(), product.getPromotions());
        }
        else {
            return new GenericProductDetailResult(null);
        }
    }

    @Override
    public ProductDetailResult getDetail(String productId, Map<String, String> variantAttributes) throws ECommerceException {
        FunctionImportClientQuery.Builder queryBuilder = new FunctionImportClientQuery.Builder()
                .withFunctionName("ProductVariant")
                .withEntityType(ODataCart.class)
                .withFunctionParameter("productId", "'" + productId + "'");
        if ( variantAttributes != null ) {
            StringBuilder variantAttributesStr = new StringBuilder();
            int i = 0;
            for ( String attributeId : variantAttributes.keySet() ) {
                String attributeValue = variantAttributes.get(attributeId);
                variantAttributesStr.append(attributeId + "=" + attributeValue);
                if (++i < variantAttributes.size()) {
                    variantAttributesStr.append("&");
                }
            }
            queryBuilder = queryBuilder.withFunctionParameter("variantAttributes", "'" + variantAttributesStr + "'");
        }
        ODataClientQuery oDataQuery = queryBuilder.build();
        ODataProduct product = (ODataProduct) this.odataClient.getEntity(oDataQuery);
        if ( product != null ) {
            return new GenericProductDetailResult(product, product.getBreadcrumbs(), product.getPromotions());
        }
        else {
            return new GenericProductDetailResult(null);
        }
    }
}
