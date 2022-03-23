using SDL.ECommerce.Api;
using System;
using SDL.ECommerce.Api.Service;
using RestSharp;
using SDL.ECommerce.Rest.Service;

namespace SDL.ECommerce.Rest
{
    public class ECommerceClient : IECommerceClient
    {
        private readonly Func<Type, object> dependencies;
        private readonly IRestClient restClient;
        private readonly IECommerceCacheProvider cacheProvider;
        private readonly int categoryExpiryTimeout;
        private readonly bool useSanitizedPathNames;
        private string environment;

        private IProductCategoryService categoryService;
        private IProductDetailService productDetailService;
        private IProductQueryService productQueryService;
        private ICartService cartService;
        private IEditService editService;

        public ECommerceClient(string endpointAddress, 
                               string locale, 
                               IECommerceCacheProvider cacheProvider,
                               int categoryExpiryTimeout,
                               bool useSanitizedPathNames,
                               Func<Type, object> dependencies = null,
                               string environment = null)
        {
            this.dependencies = dependencies;
            this.environment = environment;
            
            this.cacheProvider = cacheProvider;
            this.categoryExpiryTimeout = categoryExpiryTimeout;
            this.useSanitizedPathNames = useSanitizedPathNames;

            var restClientFactory = Resolve<RestClientFactory>() ?? new RestClientFactory();
            restClient = restClientFactory.CreateClient(endpointAddress, locale);
        }

        public ICartService CartService
        {
            get
            {
                if ( cartService == null)
                {
                    cartService = Resolve<ICartService>() ?? new CartService(this.restClient);
                }
                return cartService;
            }
        }

        public IProductCategoryService CategoryService
        {
            get
            {
                if ( categoryService == null )
                {
                    categoryService = Resolve<IProductCategoryService>() ?? new ProductCategoryService(this.restClient, this.categoryExpiryTimeout, this.useSanitizedPathNames); 
                }
                return categoryService;
            }
        }

        public IProductDetailService DetailService
        {
            get
            {
               if ( productDetailService == null)
                {
                    productDetailService = Resolve<IProductDetailService>() ?? new ProductDetailService(this.restClient, CategoryService, this.cacheProvider, environment);
                }
                return productDetailService;
            }
        }

        public IEditService EditService
        {
            get
            {
                if ( editService == null)
                {
                    editService = Resolve<IEditService>() ?? new EditService(this.restClient, this.cacheProvider, environment);
                }
                return editService;
            }
        }

        public IProductQueryService QueryService
        {
            get
            {
                if ( productQueryService == null )
                {
                    productQueryService = Resolve<IProductQueryService>() ?? new ProductQueryService(this.restClient, this.cacheProvider, environment);
                }
                return productQueryService;
            }
        }

        private T Resolve<T>()
        {
            var dependency = dependencies?.Invoke(typeof(T));

            if (dependency == null)
            {
                return default(T);
            }

            return (T)dependency;
        }
    }
}
