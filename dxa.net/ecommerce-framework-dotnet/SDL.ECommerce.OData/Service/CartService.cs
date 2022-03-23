using SDL.ECommerce.Api.Service;

using System.Linq;

using SDL.ECommerce.Api.Model;
using Sdl.Web.Delivery.Service;

namespace SDL.ECommerce.OData
{
    public class CartService : ICartService
    {
        private ECommerceClient ecommerceClient;

        internal CartService(ECommerceClient ecommerceClient)
        {
            this.ecommerceClient = ecommerceClient;
        }

        public ICart CreateCart()
        {
            ODataV4ClientFunction func = new ODataV4ClientFunction("CreateCart");
            func.AllowCaching = false;
            return Enumerable.FirstOrDefault<Cart>(this.ecommerceClient.ODataV4Service.Execute<Cart>(func));
        }

        public ICart AddProductToCart(ICart cart, string productId, int quantity)
        {
            ODataV4ClientFunction func = new ODataV4ClientFunction("AddProductToCart");
            func.AllowCaching = false;
            func.WithParam("cartId", cart.Id);
            func.WithParam("sessionId", cart.SessionId);
            func.WithParam("productId", productId);
            func.WithParam("quantity", quantity);
            return Enumerable.FirstOrDefault<Cart>(this.ecommerceClient.ODataV4Service.Execute<Cart>(func));
        }

        public ICart RemoveProductFromCart(ICart cart, string productId)
        {
            ODataV4ClientFunction func = new ODataV4ClientFunction("RemoveProductFromCart");
            func.AllowCaching = false;
            func.WithParam("cartId", cart.Id);
            func.WithParam("sessionId", cart.SessionId);
            func.WithParam("productId", productId);
            return Enumerable.FirstOrDefault<Cart>(this.ecommerceClient.ODataV4Service.Execute<Cart>(func));
        }
    }
}
