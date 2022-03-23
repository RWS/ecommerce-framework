using SDL.ECommerce.Api.Service;
using System;
using System.Collections.Generic;
using System.Linq;
using SDL.ECommerce.Api.Model;
using RestSharp;
using SDL.ECommerce.Rest.Model;
using SDL.ECommerce.Formatting.Servants;

namespace SDL.ECommerce.Rest.Service
{
    /// <summary>
    /// E-Commerce Category Service
    /// </summary>
    class ProductCategoryService : IProductCategoryService
    {
        private IRestClient restClient;
        private int categoryExpiryTimeout = 3600000;
        private bool useSanitizedPathNames = false;
        private ICategory rootCategory = new Category();
        private readonly ISanitizerServant _sanitizerServant;

        public ProductCategoryService(IRestClient restClient, int categoryExpiryTimeout, bool useSanitizedPathNames) 
        {
            this.restClient = restClient;
            this.useSanitizedPathNames = useSanitizedPathNames;
            _sanitizerServant = new SanitizerServant(new SanitizerConfiguration());
            this.GetTopLevelCategories();
            this.categoryExpiryTimeout = categoryExpiryTimeout;            
        }

        /// <summary>
        /// Get top level categories from the product catalog.
        /// </summary>
        /// <returns></returns>
        public IList<ICategory> GetTopLevelCategories()
        {
            if (((Category)rootCategory).NeedRefresh())
            {
                LoadCategories(rootCategory);
            }
            return rootCategory.Categories;
        }

        /// <summary>
        /// Get a specific category by identity.
        /// </summary>
        /// <param name="id"></param>
        /// <returns></returns>
        public ICategory GetCategoryById(string id)
        {
            // First recursively check in cache
            //
            var category = GetCategoryById(id, rootCategory.Categories, true);
            if (category == null)
            {
                // Secondly get the category and try to fit it into the cached structure
                //
                category = GetCategoryFromService(id);
                ICategory currentParent = rootCategory;
                var parentIds = ((Category)category).ParentIds;
                if (parentIds != null)
                {
                    foreach (var parentId in parentIds)
                    {
                        var parent = GetCategoryById(parentId, currentParent.Categories);
                        if (parent == null)
                        {
                            // If something has changed with the category tree since last cache update
                            //
                            LoadCategories(currentParent);
                            parent = GetCategoryById(parentId, currentParent.Categories);
                            if (parent == null)
                            {
                                throw new Exception("Inconsistent data returned from single category request and category tree requests.");
                            }
                        }
                        currentParent = parent;
                        if (parent.Categories == null)
                        {
                            LoadCategories(parent);
                        }

                        // If last item in the list -> Use that as parent reference for the current category
                        //
                        if (parentIds.IndexOf(parentId) == parentIds.Count - 1)
                        {
                            ((Category)category).SetParent(parent);
                            LoadCategories(category);
                        }
                    }
                }
            }
            return category;

        }

        /// <summary>
        /// Get a specific category by path.
        /// </summary>
        /// <param name="path"></param>
        /// <returns></returns>
        public ICategory GetCategoryByPath(string path)
        {
            if (String.IsNullOrEmpty(path) || path.Equals("/"))
            {
                return rootCategory;
            }
            var tokens = path.Split(new char[] { '/' }, StringSplitOptions.RemoveEmptyEntries);
            ICategory category = null;
            int index = 0;
            while (index < tokens.Count())
            {
                var pathName = tokens[index++];
                if (category == null)
                {
                    category = GetCategoryByPathName(rootCategory.Categories, pathName);
                }
                else
                {
                    if (((Category)category).NeedRefresh())
                    {
                        LoadCategories(category);
                    }
                    category = GetCategoryByPathName(category.Categories, pathName);
                }
                if (category == null)
                {
                    return null;
                }
            }
            if (category != null && ((Category)category).NeedRefresh())
            {
                LoadCategories(category);
            }
            return category;

        }

        /// <summary>
        /// Load subordinated categories for specific category
        /// </summary>
        /// <param name="parent"></param>
        internal void LoadCategories(ICategory parent)
        {
            IList<ICategory> categories;
            if (parent == rootCategory)
            {
                categories = GetCategoriesFromService();
            }
            else
            {
                categories = GetCategoriesFromService(parent.Id);
            }
            IList<ICategory> existingCategories = parent.Categories;
            IList<ICategory> newCategoryList = new List<ICategory>();

            for (int i = 0; i < categories.Count; i++)
            {
                var category = categories[i];
                var existingCategory = GetCategory(category.Id, existingCategories);
                if (existingCategory != null)
                {
                    ((Category)existingCategory).SetParent(parent);
                    newCategoryList.Add(existingCategory);
                }
                else
                {
                    ((Category)category).SetParent(parent);

                    if (this.useSanitizedPathNames)
                    {
                        ((Category)category).SanitizedPathName = SanitizePathName(category.PathName);
                    }

                    newCategoryList.Add(category);
                }
            }

            lock (parent)
            {
                ((Category)parent).SetCategories(newCategoryList, (DateTime.UtcNow.Ticks / TimeSpan.TicksPerMillisecond) + categoryExpiryTimeout);
            }

        }

        /// <summary>
        /// Sanitize a path name
        /// </summary>
        /// <param name="categoryId"></param>
        /// <returns></returns>
        private string SanitizePathName(string pathName)
        {
            return _sanitizerServant.SanitizedUrlString(pathName);
        }

        /// <summary>
        /// Get categories from service
        /// </summary>
        /// <param name="categoryId"></param>
        /// <returns></returns>
        private IList<ICategory> GetCategoriesFromService(string categoryId = null)
        {
            var request = new RestRequest("/category/" + (categoryId != null ? categoryId : ""), Method.GET);
            var response = this.restClient.Execute<List<Category>>(request);
            if (response.StatusCode == System.Net.HttpStatusCode.OK)
            {
                return response.Data.Cast<ICategory>().ToList();
            }
            throw new Exception("Category can not loaded. Error Code: " + response.StatusCode + ", Error Message: " + response.ErrorMessage);
        }

        /// <summary>
        /// Get categories from service
        /// </summary>
        /// <param name="categoryId"></param>
        /// <returns></returns>
        private ICategory GetCategoryFromService(string categoryId)
        {
            var request = new RestRequest("/category/" + categoryId + "?single=true", Method.GET);
            var response = this.restClient.Execute<Category>(request);
            return response.Data;
        }

        /// <summary>
        /// Get category by Id via the cache. If setting the optional parameter 'refresh=true', then checks
        /// will be done on the categories while navigating if anyone needs to be refreshed.
        /// </summary>
        /// <param name="id"></param>
        /// <param name="categories"></param>
        /// <param name="refresh"></param>
        /// <returns></returns>
        private ICategory GetCategoryById(String id, IList<ICategory> categories, bool refresh = false)
        {
            if (categories != null)
            {
                foreach (var category in categories)
                {
                    if (category.Id.Equals(id))
                    {
                        return category;
                    }
                    if (category.Categories != null)
                    {
                        if (refresh && ((Category)category).NeedRefresh())
                        {
                            LoadCategories(category);
                        }
                        ICategory foundCategory = GetCategoryById(id, category.Categories);
                        if (foundCategory != null)
                        {
                            return foundCategory;
                        }
                    }
                }
            }

            return null;
        }

        /// <summary>
        /// Get category by relative pathname
        /// </summary>
        /// <param name="categories"></param>
        /// <param name="pathName"></param>
        /// <returns></returns>
        private ICategory GetCategoryByPathName(IList<ICategory> categories, String pathName)
        {
            foreach (var category in categories)
            {
                if (this.useSanitizedPathNames)
                {
                    if (category.SanitizedPathName != null && category.SanitizedPathName.Equals(pathName))
                    {
                        return category;
                    }
                }
                else
                {
                    if (category.PathName.Equals(pathName))
                    {
                        return category;
                    }
                }
            }
            return null;
        }

        /// <summary>
        /// Get category with specific ID from provided category list.
        /// </summary>
        /// <param name="id"></param>
        /// <param name="categories"></param>
        /// <returns></returns>
        private ICategory GetCategory(String id, IList<ICategory> categories)
        {
            if (categories != null)
            {
                foreach (var category in categories)
                {
                    if (category.Id.Equals(id))
                    {
                        return category;
                    }
                }
            }
            return null;
        }
    }
}
