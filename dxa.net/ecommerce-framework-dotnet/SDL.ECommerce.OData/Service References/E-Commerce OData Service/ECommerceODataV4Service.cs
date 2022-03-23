using Microsoft.OData.Client;

using Sdl.Web.Delivery.Service;

namespace SDL.ECommerce.OData
{
    public class ECommerceODataV4Service : ODataV4Service, IECommerceODataV4Service
    {
        public ECommerceODataV4Service(SDLECommerce service, IServiceConfiguration serviceConfig)
            : base(service, serviceConfig)
        {
        }

        public ECommerceODataV4Service(SDLECommerce service, IServiceConfiguration serviceConfig, IOAuthTokenProvider tokenProvider)
            : base(service, serviceConfig, tokenProvider)
        {
        }

        public DataServiceQuery<Category> Categories => (Service as SDLECommerce)?.Categories;

        public DataServiceQuery<Product> Products => (Service as SDLECommerce)?.Products;

        public DataServiceQuery<Cart> Carts => (Service as SDLECommerce)?.Carts;

        public DataServiceQuery<ProductQueryResult> ProductQueryResults => (Service as SDLECommerce)?.ProductQueryResults;

        public DataServiceQuery<EditMenu> EditMenus => (Service as SDLECommerce)?.EditMenus;
    }
}