namespace SDL.ECommerce.UnitTests.Fakes
{
    using System;
    using System.Collections.Concurrent;
    using System.Collections.Generic;
    using ECommerce.Api;

    public class FakeECommerceCacheProvider : IECommerceCacheProvider
    {
        private IDictionary<Tuple<CacheRegion, string>, object> _cache = new ConcurrentDictionary<Tuple<CacheRegion, string>, object>();

        public void Store<T>(CacheRegion region, string key, T value)
        {
            _cache[new Tuple<CacheRegion, string>(region, key)] = value;
        }

        public bool TryGet<T>(CacheRegion region, string key, out T value)
        {
            object val;

            if (_cache.TryGetValue(new Tuple<CacheRegion, string>(region, key), out val))
            {
                value = (T) val;

                return true;
            }

            value = default(T);

            return false;
        }
    }
}