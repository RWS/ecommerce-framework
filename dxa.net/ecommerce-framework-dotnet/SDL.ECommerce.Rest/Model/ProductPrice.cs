using SDL.ECommerce.Api.Model;

namespace SDL.ECommerce.Rest.Model
{
    public class ProductPrice : IProductPrice
    {
        public string Currency { get; set; }
        public string FormattedPrice { get; set; }
        public float? Price { get; set; }
    }
}
