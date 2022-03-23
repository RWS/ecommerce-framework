using Sdl.Web.Common.Configuration;
using Sdl.Web.Common.Interfaces;
using SDL.ECommerce.Api;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;

namespace SDL.ECommerce.DXA
{
    /// <summary>
    /// DXA Cache provider that wraps the OOTB DXA cache provider (that uses MemCache, REDIS etc)
    /// </summary>
    public class DXACacheProvider : IECommerceCacheProvider
    {
        private ICacheProvider _cacheProvider;
        private string _locale;

        public DXACacheProvider(string locale)
        {
            _cacheProvider = SiteConfiguration.CacheProvider;
            _locale = locale;
        }

        public void Store<T>(CacheRegion region, string key, T value)
        {
            _cacheProvider.Store(key + "#" + _locale, region.ToString(), value);
        }

        public bool TryGet<T>(CacheRegion region, string key, out T value)
        {
            return _cacheProvider.TryGet(key + "#" + _locale, region.ToString(), out value);
        }
    }
}