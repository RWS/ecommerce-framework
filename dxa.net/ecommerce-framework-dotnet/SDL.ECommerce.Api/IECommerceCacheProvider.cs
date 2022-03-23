using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace SDL.ECommerce.Api
{
    /// <summary>
    /// Cache Region
    /// </summary>
    public enum CacheRegion
    {
        ECommerceProductDetail,
        ECommerceProductLister,
        ECommerceSearch,
        ECommerceFlyout,
        ECommerceInContextMenu
    }

    /// <summary>
    /// E-Commerce Cache Provider. 
    /// Can be specific for each type of E-Commerce entity (category, producy) and operation (lister, search)
    /// </summary>
    public interface IECommerceCacheProvider
    {
        /// <summary>
        /// Stores a given key/value pair to the cache
        /// </summary>
        /// <typeparam name="T"></typeparam>
        /// <param name="region"></param>
        /// <param name="key"></param>
        /// <param name="value"></param>
        void Store<T>(CacheRegion region, string key, T value);

        /// <summary>
        /// Tries to get a cached value for a given key
        /// </summary>
        /// <typeparam name="T"></typeparam>
        /// <param name="region"></param>
        /// <param name="key"></param>
        /// <param name="value"></param>
        /// <returns></returns>
        bool TryGet<T>(CacheRegion region, string key, out T value);
    }
}
