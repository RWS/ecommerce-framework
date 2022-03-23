namespace SDL.ECommerce.DXA.Servants
{
    using System;
    using System.Collections.Generic;
    using System.Linq;

    using SDL.ECommerce.Api.Model;

    public class PathServant : IPathServant
    {
        private const string CATEGORIES_PATH = "/categories/";

        private const string PRODUCTS_PATH = "/products/";

        private const string GENERIC_PATH = "generic";

        private const string CATEGORY_PATH_SEPARATOR = "-";
        
        public IEnumerable<string> GetSearchPath(string url, ICategory category)
        {
            var searchPaths = new List<string>();

            var basePath = ECommerceContext.LocalizePath(CATEGORIES_PATH);

            var categoryPath = basePath;

            var currentCategory = category;

            while (currentCategory != null)
            {
                searchPaths.Add(categoryPath + currentCategory.Id);

                currentCategory = currentCategory.Parent;
            }

            var urlTokens = url.Split(new[] { '/' }, StringSplitOptions.RemoveEmptyEntries);

            foreach (var token in urlTokens)
            {
                categoryPath += token;

                searchPaths.Insert(0, categoryPath);

                categoryPath += CATEGORY_PATH_SEPARATOR;
            }

            searchPaths.Add(basePath + GENERIC_PATH);

            return searchPaths;
        }

        public IEnumerable<string> GetSearchPath(string productSeoId, IProduct product)
        {
            var searchPath = new List<string>();

            var basePath = ECommerceContext.LocalizePath(PRODUCTS_PATH);

            if (!string.IsNullOrEmpty(productSeoId))
            {
                searchPath.Add(basePath + productSeoId);
            }

            searchPath.Add(basePath + product.Id);

            if (product.Categories != null)
            {
                searchPath.AddRange(product.Categories.Select(category => basePath + category.Id));
            }

            searchPath.Add(basePath + GENERIC_PATH);

            return searchPath;
        }
    }
}