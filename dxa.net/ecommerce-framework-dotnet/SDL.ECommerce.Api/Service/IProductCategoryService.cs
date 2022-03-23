using SDL.ECommerce.Api.Model;

using System.Collections.Generic;

namespace SDL.ECommerce.Api.Service
{
    /// <summary>
    /// E-Commerce Product Category Service
    /// </summary>
    public interface IProductCategoryService
    {

        /// <summary>
        /// Get top level categories.
        /// </summary>
        /// <returns></returns>
        IList<ICategory> GetTopLevelCategories();

        /// <summary>
        /// Get category by ID.
        /// </summary>
        /// <returns></returns>
        ICategory GetCategoryById(string id);

        /// <summary>
        /// et category by path, e.g. /category1/category2.
        /// Is based on the category name itself to provide a SEO friendly URL to categories.
        /// </summary>
        /// <param name="id"></param>
        /// <returns></returns>
        ICategory GetCategoryByPath(string path);
    }
}
