package com.sdl.ecommerce.odata.function;

import com.sdl.ecommerce.api.ProductDetailResult;
import com.sdl.ecommerce.api.model.Product;
import com.sdl.ecommerce.odata.datasource.ProductDataSource;
import com.sdl.ecommerce.odata.model.ODataProduct;
import com.sdl.ecommerce.odata.service.ODataRequestContextHolder;
import com.sdl.odata.api.ODataException;
import com.sdl.odata.api.edm.annotations.EdmFunction;
import com.sdl.odata.api.edm.annotations.EdmParameter;
import com.sdl.odata.api.edm.annotations.EdmProperty;
import com.sdl.odata.api.edm.annotations.EdmReturnType;
import com.sdl.odata.api.edm.model.Operation;
import com.sdl.odata.api.processor.datasource.ODataEntityNotFoundException;
import com.sdl.odata.api.processor.datasource.factory.DataSourceFactory;
import com.sdl.odata.api.service.ODataRequestContext;

import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * ProductVariantFunction
 *
 * @author nic
 */
@EdmFunction(
        name = "ProductVariantFunction",
        namespace = "SDL.ECommerce"
)
@EdmReturnType(
        type = "Products"
)
public class ProductVariantFunction implements Operation<ODataProduct> {

    @EdmParameter(nullable = false)
    private String productId;

    @EdmParameter(nullable = true)
    private String variantAttributes;

    @Override
    public ODataProduct doOperation(ODataRequestContext oDataRequestContext, DataSourceFactory dataSourceFactory) throws ODataException {
        // TODO: Use the generic ECommerceOperation base class here as well
        ODataRequestContextHolder.set(oDataRequestContext);
        try {
            ProductDataSource productDataSource = (ProductDataSource) dataSourceFactory.getDataSource(oDataRequestContext, "SDL.ECommerce.Product");
            Map<String, String> variantAttributeMap = null;
            if (variantAttributes != null) {
                variantAttributeMap = new HashMap<>();
                StringTokenizer tokenizer = new StringTokenizer(variantAttributes, "=&");
                while (tokenizer.hasMoreTokens()) {
                    String attributeId = tokenizer.nextToken();
                    String attributeValue = tokenizer.nextToken();
                    variantAttributeMap.put(attributeId, attributeValue);
                }
            }

            ProductDetailResult result = productDataSource.getProductDetailService().getDetail(productId, variantAttributeMap);
            if (result != null && result.getProductDetail() != null) {
                return new ODataProduct(result);
            }
            throw new ODataEntityNotFoundException("Product with ID '" + productId + "' and variant attributes '" + variantAttributes + "' not found.");
        }
        finally {
            ODataRequestContextHolder.clear();
        }
    }
}
