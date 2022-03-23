using SDL.ECommerce.Api;
using SDL.ECommerce.Api.Service;

namespace SDL.ECommerce.UnitTests.Dxa.Fakes
{
    public class FakeECommerceClient : IECommerceClient
    {
        public IProductCategoryService CategoryService { get; set; }

        public IProductQueryService QueryService { get; set; }

        public IProductDetailService DetailService { get; set; }

        public ICartService CartService { get; set; }

        public IEditService EditService { get; set; }
    }
}