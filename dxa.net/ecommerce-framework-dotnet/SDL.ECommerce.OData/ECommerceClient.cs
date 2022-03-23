using Sdl.Web.Delivery.DiscoveryService;
using Sdl.Web.Delivery.Service;
using SDL.ECommerce.Api;
using SDL.ECommerce.Api.Service;
using System;

namespace SDL.ECommerce.OData
{
    // Temporary solution for the client config
    // TODO: Use the standard approach
    //
    class SimpleServiceConfiguration : IServiceConfiguration
    {

        public string ClientId
        {
            get
            {
                throw new NotImplementedException();
            }
        }

        public string ClientSecret
        {
            get
            {
                throw new NotImplementedException();
            }
        }

        public bool IsOAuthEnabled
        {
            get
            {
                return false;
            }
        }

        public Uri ServiceEndpoint
        {
            get; set;
            
        }

        public int Timeout
        {
            get; set;
        }

        public Uri TokenServiceEndpoint
        {
            get
            {
                throw new NotImplementedException();
            }
        }
    }

    /// <summary>
    /// ECommerce Client. Is the single entry point for all kind interactions with the E-Commerce OData micro service.
    /// </summary>
    public class ECommerceClient : IECommerceClient
    {
        private readonly Func<Type, object> dependencies;
        private readonly IServiceConfiguration serviceConfiguration;
        private IProductCategoryService categoryService;
        private IProductQueryService queryService;
        private IProductDetailService detailService;
        private ICartService cartService;
        private IEditService editService;
        private readonly bool useSanitizedPathNames;

        /// <summary>
        /// Constructor
        /// </summary>
        /// <param name="endpointAddress">Endpoing address for the ECommerce OData service.</param>
        /// <param name="locale">Current locale.</param>
        /// <param name="dependencies">Optional list of dependencies.</param>
        public ECommerceClient(
            string endpointAddress, 
            string locale,
            bool useSanitizedPathNames,
            Func<Type, object> dependencies = null)
        {
            this.serviceConfiguration = new SimpleServiceConfiguration
                                            {
                                                ServiceEndpoint = new Uri(endpointAddress + "/" + locale),
                                                Timeout = 1000
                                            };

            this.useSanitizedPathNames = useSanitizedPathNames;
            this.dependencies = dependencies;
        }
        
        /// <summary>
        /// Get category service
        /// </summary>
        public IProductCategoryService CategoryService
        {
            get
            {
                if (categoryService == null)
                {
                    categoryService = this.Resolve<IProductCategoryService>() ?? new ProductCategoryService(this, this.useSanitizedPathNames);
                }

                return categoryService;
            }
        }

        /// <summary>
        /// Get query service
        /// </summary>
        public IProductQueryService QueryService
        {
            get
            {
                if (queryService == null)
                {
                    queryService = this.Resolve<IProductQueryService>() ?? new ProductQueryService(this);
                }

                return queryService;
            }
        }

        /// <summary>
        /// Get product detail service
        /// </summary>
        public IProductDetailService DetailService
        {
            get
            {
                if ( detailService == null )
                {
                    detailService = this.Resolve<IProductDetailService>() ?? new ProductDetailService(this);
                }

                return detailService;
            }
        }

        /// <summary>
        /// Get cart service
        /// </summary>
        public ICartService CartService
        {
            get
            {
                if ( cartService == null )
                {
                    cartService = this.Resolve<ICartService>() ?? new CartService(this);
                }

                return cartService;
            }
        }

        /// <summary>
        /// Get edit service
        /// </summary>
        public IEditService EditService
        {
            get
            {
                if (editService == null)
                {
                    editService = this.Resolve<IEditService>() ?? new EditService(this);
                }

                return editService;
            }
        }

        private IOAuthTokenProvider TokenProvider
        {
            get
            {
                return this.Resolve<IOAuthTokenProvider>() ?? DiscoveryServiceProvider.DefaultTokenProvider;
            }
        }

        /// <summary>
        /// Get new OData V4 service instance
        /// </summary>
        public IECommerceODataV4Service ODataV4Service
        {
            get
            {
                return this.Resolve<IECommerceODataV4Service>() ?? new ECommerceODataV4Service(new SDLECommerce(serviceConfiguration.ServiceEndpoint), serviceConfiguration, TokenProvider);
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