package com.sdl.ecommerce.odata.function;

import com.sdl.ecommerce.api.Query;
import com.sdl.ecommerce.api.edit.EditMenu;
import com.sdl.ecommerce.api.edit.EditService;
import com.sdl.ecommerce.api.model.Category;
import com.sdl.ecommerce.odata.datasource.ProductDataSource;
import com.sdl.ecommerce.odata.model.ODataEditMenu;
import com.sdl.ecommerce.odata.service.ODataRequestContextHolder;
import com.sdl.odata.api.ODataException;
import com.sdl.odata.api.edm.annotations.EdmFunction;
import com.sdl.odata.api.edm.annotations.EdmParameter;
import com.sdl.odata.api.edm.annotations.EdmReturnType;
import com.sdl.odata.api.edm.model.Operation;
import com.sdl.odata.api.processor.datasource.factory.DataSourceFactory;
import com.sdl.odata.api.service.ODataRequestContext;

/**
 * GetInContextEditMenuFunction
 *
 * @author nic
 */
@EdmFunction(
        name = "GetInContextEditMenuFunction",
        namespace = "SDL.ECommerce"
        )
@EdmReturnType(
        type = "EditMenus"
)
public class GetInContextEditMenuFunction implements Operation<ODataEditMenu> {

    @EdmParameter
    private String categoryId;

    @EdmParameter
    private String searchPhrase;

    @Override
    public ODataEditMenu doOperation(ODataRequestContext oDataRequestContext, DataSourceFactory dataSourceFactory) throws ODataException {
        ODataRequestContextHolder.set(oDataRequestContext);
        try {
            ProductDataSource productDataSource = (ProductDataSource) dataSourceFactory.getDataSource(oDataRequestContext, "SDL.ECommerce.Product");
            Query query = productDataSource.getProductQueryService().newQuery();
            if (categoryId != null) {
                Category category = productDataSource.getProductCategoryService().getCategoryById(categoryId);
                query.category(category);
            }
            if (searchPhrase != null) {
                query.searchPhrase(searchPhrase);
            }
            EditService editService = productDataSource.getEditService();
            if (editService != null) {
                EditMenu editMenu = productDataSource.getEditService().getInContextMenuItems(query);
                return new ODataEditMenu(editMenu, query);
            }
            return null;
        }
        finally {
            ODataRequestContextHolder.clear();
        }
    }
}
