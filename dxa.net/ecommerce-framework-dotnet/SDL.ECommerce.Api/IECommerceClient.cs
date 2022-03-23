using SDL.ECommerce.Api.Service;

namespace SDL.ECommerce.Api
{
    public interface IECommerceClient
    {
        IProductCategoryService CategoryService { get; }
        IProductQueryService QueryService { get; }
        IProductDetailService DetailService { get; }
        ICartService CartService { get; }
        IEditService EditService { get; }
    }
}
