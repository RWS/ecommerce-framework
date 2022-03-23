using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Net;
using System.Runtime.Caching;
using Tridion.ExternalContentLibrary.V2;

namespace SDL.ECommerce.Ecl
{
    /// <summary>
    /// Mountpoint base class
    /// </summary>
    abstract public class Mountpoint : IContentLibraryContext
    {

        protected IEclSession session;

        protected Mountpoint(IEclSession session)
        {
            this.session = session;
        }

        public bool CanGetUploadMultimediaItemsUrl(int publicationId)
        {
            return true;
        }

        public virtual bool CanSearch(int publicationId)
        {
            return false;
        }

        public IFolderContent Search(IEclUri contextUri, string searchTerm, int pageIndex, int numberOfItems)
        {
            string categoryId = null;
            if ( contextUri.ItemType == EclItemTypes.Folder && !contextUri.ItemId.StartsWith("Type_") )
            {
                categoryId = contextUri.ItemId;
            }
            List<IContentLibraryListItem> items = new List<IContentLibraryListItem>();
            // TODO: Override number of items in result
            // TODO: Add support for pagination here
            var result = EclProvider.ProductCatalog.Search(searchTerm, categoryId, contextUri.PublicationId, pageIndex);
            if (result != null)
            {
                foreach (var product in result.Products)
                {
                    items.Add(this.CreateProductItem(contextUri.PublicationId, null, product));
                    AddProductToCache(product);
                }
            }
            return EclProvider.HostServices.CreateFolderContent(contextUri, pageIndex, result.NumberOfPages, items, CanGetUploadMultimediaItemsUrl(contextUri.PublicationId), CanSearch(contextUri.PublicationId));
        }

        public IList<IContentLibraryListItem> FindItem(IEclUri eclUri)
        {
            // return null so we force it to call GetItem(IEclUri)
            //
            return null;
        }

        public IFolderContent GetFolderContent(IEclUri parentFolderUri, int pageIndex, EclItemTypes itemTypes)
        {
            List<IContentLibraryListItem> items = new List<IContentLibraryListItem>();
            int numberOfPages = 1;

            // If root -> list all root categories
            //
            if (parentFolderUri.ItemType == EclItemTypes.MountPoint && itemTypes.HasFlag(EclItemTypes.Folder))
            {
                items.Add(new TypeItem(parentFolderUri.PublicationId, "Categories"));
                items.Add(new TypeItem(parentFolderUri.PublicationId, "Products"));  
            }
            else if (parentFolderUri.ItemType == EclItemTypes.Folder && itemTypes.HasFlag(EclItemTypes.Folder))
            {
                if (parentFolderUri.ItemId.StartsWith("Type_") )
                {
                    if (parentFolderUri.ItemId.Equals("Type_Products"))
                    {
                        foreach (var category in EclProvider.GetRootCategory(parentFolderUri.PublicationId).Categories)
                        {
                            if (!String.IsNullOrEmpty(category.CategoryId))
                            {
                                items.Add(new CategoryItem(parentFolderUri.PublicationId, category));
                            }
                        }
                    }
                    else // Type_Categories
                    {
                        // TODO: Can we somehow build up a structure here instead???
                        // TODO: Have a hook for providers to hook in their variant on the listing here???

                        if (itemTypes.HasFlag(EclItemTypes.File))
                        {
                            var rootCategory = EclProvider.GetRootCategory(parentFolderUri.PublicationId);
                            var flattenList = new CategoryFlattenPaginatedList(rootCategory, EclProvider.CategoryPageSize, EclProvider.CategoryMaxDepth);
                            var categories = flattenList.Next(pageIndex);
                            foreach (var category in categories)
                            {
                                items.Add(new SelectableCategoryItem(parentFolderUri.PublicationId, category));
                            }

                            if (categories.Count() == EclProvider.CategoryPageSize)
                            {
                                // As we do not know exactly how many categories there are so can't we give an exact figure on number of pages.
                                //
                                numberOfPages = pageIndex + 2;
                            }
                            else
                            {
                                numberOfPages = pageIndex + 1; // Reached the last page
                            }
                        }
                    }
                }
                else
                {
                    var parentCategory = new DummyCategory(parentFolderUri.ItemId);
                    var result = EclProvider.ProductCatalog.GetCategoryAndProducts(parentFolderUri.ItemId, parentFolderUri.PublicationId, pageIndex);
                    if (result != null)
                    {
                        numberOfPages = result.NumberOfPages;
                        if (itemTypes.HasFlag(EclItemTypes.Folder))
                        {
                            foreach (var category in result.Categories)
                            {
                                items.Add(new CategoryItem(parentFolderUri.PublicationId, category));
                            }
                        }
                        if (itemTypes.HasFlag(EclItemTypes.File))
                        {
                            foreach (var product in result.Products)
                            {
                                items.Add(this.CreateProductItem(parentFolderUri.PublicationId, parentCategory, product));
                                AddProductToCache(product);
                            }
                        }
                    }
                }
            }
            
            return EclProvider.HostServices.CreateFolderContent(parentFolderUri, pageIndex, numberOfPages, items, CanGetUploadMultimediaItemsUrl(parentFolderUri.PublicationId), CanSearch(parentFolderUri.PublicationId));
         
        }

        /// <summary>
        /// Create new product ECL item. Needs to be implemented by concrete subclass.
        /// </summary>
        /// <param name="publicationId"></param>
        /// <param name="parentCategory"></param>
        /// <param name="product"></param>
        /// <returns></returns>
        protected abstract ProductItem CreateProductItem(int publicationId, Category parentCategory, Product product);

        public IContentLibraryItem GetItem(IEclUri eclUri)
        {
            if (eclUri.ItemType == EclItemTypes.File )
            {
                if ( eclUri.SubType.Equals("product") )
                {
                    string productId = eclUri.ItemId;
                    return this.CreateProductItem(eclUri.PublicationId, null, GetProductFromCacheOrCatalog(productId, eclUri.PublicationId));
                }
                else // selectable category
                {
                    string categoryId = eclUri.ItemId;

                    // First try to see if it has been cached since before
                    //
                    var rootCategory = EclProvider.GetRootCategory(eclUri.PublicationId);
                    Category category = null;
                    if (rootCategory != null)
                    {
                        category = rootCategory.GetCachedCategory(categoryId);
                    }
                    if (category == null)
                    {
                        // Get the category from the product catalog
                        //
                        category = EclProvider.GetCategory(categoryId, eclUri.PublicationId);
                    }
                    if (category == null)
                    {
                        // Category could not be found. It has probably been removed from the external system, but references might still
                        // exists in Tridion. In that case we need to be able to return a dummy category item.
                        //
                        category = new DummyCategory(categoryId);
                    }
                    return new SelectableCategoryItem(eclUri.PublicationId, category);
                }
            }
            else if (eclUri.ItemType == EclItemTypes.Folder)
            {
                if (eclUri.ItemId.StartsWith("Type_"))
                {
                    string typeName = eclUri.ItemId.Replace("Type_", "");
                    return new TypeItem(eclUri.PublicationId, typeName); 
                }
                else
                {
                    string categoryId = eclUri.ItemId;
                    if ( categoryId == null )
                    {
                        throw new Exception("Undefined category for ECL URI: " + eclUri);
                    }
                    if (categoryId.Equals("0")) // If root category -> return the 'Products' type folder
                    {
                        return new TypeItem(eclUri.PublicationId, "Products");
                    }

                    var category = EclProvider.GetCategory(categoryId, eclUri.PublicationId);
                    if ( category == null )
                    {
                        // Use the root category as fallback to avoid errors when browsing in the ECL catalog
                        //
                        category = EclProvider.GetRootCategory(eclUri.PublicationId);
                    }

                    return new CategoryItem(eclUri.PublicationId, category);
                }
                
            }
            throw new NotSupportedException();
        }

        public IList<IContentLibraryItem> GetItems(IList<IEclUri> eclUris)
        {
            List<IContentLibraryItem> items = new List<IContentLibraryItem>();
            
            IEnumerable<string> uniqueIds = (from uri in eclUris
                                                  where uri.ItemType == EclItemTypes.File 
                                                  select uri.ItemId).Distinct();
            foreach (string id in uniqueIds)
            {
                string itemId = id;
                var urisForProduct = from uri in eclUris
                                   where uri.ItemType == EclItemTypes.File && uri.ItemId == itemId
                                   select uri;

                foreach (IEclUri eclUri in urisForProduct)
                {
                    items.Add(GetItem(eclUri));
                }
            }
            
            return items;
        }

        public byte[] GetThumbnailImage(IEclUri eclUri, int maxWidth, int maxHeight)
        {
            if ( eclUri.ItemType == EclItemTypes.File && eclUri.SubType.Equals("product") )
            {
                string productId = eclUri.ItemId;
                Product product = GetProductFromCacheOrCatalog(productId, eclUri.PublicationId);
             
                if (product != null && product.Thumbnail != null)
                {
                    using (WebClient webClient = new WebClient())
                    {
                        Stream stream = new MemoryStream(webClient.DownloadData(product.Thumbnail.Url));
                        stream.Position = 0;
                        return EclProvider.HostServices.CreateThumbnailImage(maxWidth, maxHeight, stream, null);
                    }
                }
            }
            return null;
        }

        public string GetUploadMultimediaItemsUrl(IEclUri parentFolderUri)
        {
            throw new NotSupportedException();
        }

        public string GetViewItemUrl(IEclUri eclUri)
        {
            throw new NotSupportedException();
        }

        public virtual string IconIdentifier
        {
            get { return "ecommerce"; }
        }

        public string Dispatch(string command, string payloadVersion, string payload, out string responseVersion)
        {
            throw new NotSupportedException();
        }

        public void StubComponentCreated(IEclUri eclUri, string tcmUri)
        {
        }

        public void StubComponentDeleted(IEclUri eclUri, string tcmUri)
        {
        }

        public void Dispose()
        {
        }

        protected void AddProductToCache(Product product)
        {
            MemoryCache.Default.Add(GetCacheKey(product.Id), new CachedProduct { Product = product }, DateTime.Now.AddSeconds(60));
        }

        protected void AddFullDataProductToCache(Product product)
        {
            MemoryCache.Default.Add(GetCacheKey(product.Id), new CachedProduct { Product = product, HasFullData = true }, DateTime.Now.AddSeconds(3600));
        }

        protected Product GetProductFromCacheOrCatalog(string productId, int publicationId)
        {
            Product product = null; 
            var cachedProduct = (CachedProduct) MemoryCache.Default.Get(GetCacheKey(productId));
            if (cachedProduct != null)
            {
                if (cachedProduct.HasFullData)
                {
                    product = cachedProduct.Product;
                }
                else
                {
                    cachedProduct.Requested++;
                    if (cachedProduct.Requested > 2)
                    {
                        // Cached product can be requested twice: once for the list build + get the thumbnail
                        // This to force a full read of the product when requesting the properties view of the product (which needs the full product information).
                        //
                        MemoryCache.Default.Remove(GetCacheKey(productId));
                    }
                    else
                    {
                        product = cachedProduct.Product;
                    }
                }
            }
            if (product == null)
            {
                product = EclProvider.ProductCatalog.GetProduct(productId, publicationId);
                if (product == null)
                {
                    // Product could not be found. It has probably been removed from the external system, but references might still
                    // exists in Tridion. In that case we need to be able to return a dummy product item.
                    //
                    return new DummyProduct(productId);
                }
                else
                {
                    AddFullDataProductToCache(product);
                }
            }
            return product;
           
        }

        private string GetCacheKey(string productId)
        {
            return session.TridionUser.Id + ":" + productId;
        }
    }
}
