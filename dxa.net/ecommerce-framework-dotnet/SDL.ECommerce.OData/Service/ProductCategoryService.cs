using SDL.ECommerce.Api.Model;
using SDL.ECommerce.Api.Service;
using SDL.ECommerce.Formatting.Servants;
using System;
using System.Collections.Generic;
using System.Linq;

namespace SDL.ECommerce.OData
{
    /// <summary>
    /// E-Commerce Category Service
    /// </summary>
    /// TODO: Implement an interface here
    public class ProductCategoryService : IProductCategoryService
    {
        // TODO: Use DXA Cache Provider here instead?? Or how decoupled should this module be from DXA?

        private ECommerceClient ecommerceClient;

        private int categoryExpiryTimeout = 3600000; // TODO: Have this configurable
        private bool useSanitizedPathNames = false;
        private ICategory rootCategory = new Category();
        private readonly ISanitizerServant _sanitizerServant;

        /// <summary>
        /// Constructor (only availably internally)
        /// </summary>
        /// <param name="service"></param>
        internal ProductCategoryService(ECommerceClient ecommerceClient, bool useSanitizedPathNames)
        {
            this.ecommerceClient = ecommerceClient;
            this.useSanitizedPathNames = useSanitizedPathNames;
            _sanitizerServant = new SanitizerServant(new SanitizerConfiguration());
            this.GetTopLevelCategories();            
        }

        /// <summary>
        /// Get top level categories from the product catalog.
        /// </summary>
        /// <returns></returns>
        public IList<ICategory> GetTopLevelCategories()
        {
            if (((Category) rootCategory).NeedRefresh())
            {
                LoadCategories(rootCategory, this.ecommerceClient.ODataV4Service);
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
            var service = this.ecommerceClient.ODataV4Service;
            // First recursively check in cache
            //
            var category = GetCategoryById(id, rootCategory.Categories, service, true);
            if ( category == null )
            {
                // Secondly get the category and try to fit it into the cached structure
                //
                category = service.Categories.ByKey(id).GetValue();
                ICategory currentParent = rootCategory;
                var parentIds = ((Category)category).ParentIds.ToList();

                foreach ( var parentId in parentIds )
                {
                    var parent = GetCategoryById(parentId, currentParent.Categories, service);
                    if ( parent == null )
                    {
                        // If something has changed with the category tree since last cache update
                        //
                        LoadCategories(currentParent, service);
                        parent = GetCategoryById(parentId, currentParent.Categories, service);
                        if ( parent == null )
                        {
                            throw new Exception("Inconsistent data returned from single category request and category tree requests.");
                        }
                    }
                    currentParent = parent;
                    if ( parent.Categories == null )
                    {
                        LoadCategories(parent, service);
                    }

                    // If last item in the list -> Use that as parent reference for the current category
                    //
                    if (parentIds.IndexOf(parentId) == parentIds.Count - 1)
                    {
                        ((Category)category).SetParent(parent);
                        LoadCategories(category, service);
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
            var service = this.ecommerceClient.ODataV4Service;

            if ( String.IsNullOrEmpty(path) || path.Equals("/") )
            {
                return rootCategory;
            }
            var tokens = path.Split(new char[] { '/' }, StringSplitOptions.RemoveEmptyEntries);
            ICategory category = null;
            int index = 0;
            while ( index < tokens.Count() )
            {
                var pathName = tokens[index++];
                if ( category == null )
                {
                    category = GetCategoryByPathName(rootCategory.Categories, pathName);
                }
                else
                {
                    if ( ((Category) category).NeedRefresh() )
                    {
                        LoadCategories(category, service);
                    }
                    category = GetCategoryByPathName(category.Categories, pathName);
                }
                if ( category == null )
                {
                    return null;
                }
            }
            if ( category != null && ((Category) category).NeedRefresh() )
            {
                LoadCategories(category, service);
            }
            return category;
            
        }

        /// <summary>
        /// Load subordinated categories for specific category
        /// </summary>
        /// <param name="parent"></param>
        internal void LoadCategories(ICategory parent, IECommerceODataV4Service service)
        {
            IList<Category> categories;
            if ( parent == rootCategory )
            {
                categories = service.Categories.ToList();
            }
            else
            {
                categories = service.Categories.ByKey(parent.Id).Categories.ToList();
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
                ((Category) parent).SetCategories(newCategoryList, (DateTime.UtcNow.Ticks / TimeSpan.TicksPerMillisecond) + categoryExpiryTimeout);
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
        /// Get category by Id via the cache. If setting the optional parameter 'refresh=true', then checks
        /// will be done on the categories while navigating if anyone needs to be refreshed.
        /// </summary>
        /// <param name="id"></param>
        /// <param name="categories"></param>
        /// <param name="refresh"></param>
        /// <returns></returns>
        private ICategory GetCategoryById(String id, IList<ICategory> categories, IECommerceODataV4Service service, bool refresh = false)
        {
            if ( categories != null )
            {
                foreach (var category in categories)
                {
                    if (category.Id.Equals(id))
                    {
                        return category;
                    }
                    if (category.Categories != null)
                    {
                        if ( refresh && ((Category) category).NeedRefresh() )
                        {
                            LoadCategories(category, service);
                        }
                        ICategory foundCategory = GetCategoryById(id, category.Categories, service);
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
