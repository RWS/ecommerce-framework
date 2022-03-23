using System.Collections.Generic;

namespace SDL.ECommerce.Ecl
{
    /// <summary>
    /// Product catalog interface.
    /// </summary>
    public interface ProductCatalog
    {
        /// <summary>
        /// Get all available catagories in the E-Commerce system.
        /// TODO: Improve with pagination here 
        /// </summary>
        /// <param name="publicationId"></param>
        /// <returns></returns>
        Category GetAllCategories(int publicationId = 0);

        /// <summary>
        /// Get category with specific identity.
        /// </summary>
        /// <param name="id"></param>
        /// <param name="publicationId"></param>
        /// <returns></returns>
        Category GetCategory(string id, int publicationId = 0);

        /// <summary>
        /// Get products belonging to specific catagory.
        /// Publication ID can be used to get a localized version of the products (specific language/country)
        /// </summary>
        /// <param name="categoryId"></param>
        /// <param name="publicationId"></param>
        /// <param name="pageIndex"></param>
        /// <returns></returns>
        QueryResult GetCategoryAndProducts(string categoryId, int publicationId = 0, int pageIndex = 0);

        /// <summary>
        /// Search products in specified category. If categoryId is NULL a full search is done.
        /// </summary>
        /// <param name="searchTerm"></param>
        /// <param name="categoryId"></param>
        /// <param name="publicationId"></param>
        /// <param name="pageIndex"></param>
        /// <returns></returns>
       QueryResult Search(string searchTerm, string categoryId, int publicationId = 0, int pageIndex = 0);

        /// <summary>
        /// Get product with specified product identity/SKU.
        /// Publication ID can be used to get a localized version of the product.
        /// </summary>
        /// <param name="id"></param>
        /// <param name="publicationId"></param>
        /// <returns></returns>
        Product GetProduct(string id, int publicationId = 0);

    }
}
