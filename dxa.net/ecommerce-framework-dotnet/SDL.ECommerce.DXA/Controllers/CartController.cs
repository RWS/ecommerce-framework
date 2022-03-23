using Sdl.Web.Common.Logging;

using System.Web.Mvc;

namespace SDL.ECommerce.DXA.Controllers
{
    /// <summary>
    /// Cart Controller
    /// </summary>
    public class CartController : System.Web.Mvc.Controller
    {
        /// <summary>
        /// Add Product to Cart
        /// </summary>
        /// <param name="productId"></param>
        /// <returns></returns>
        public virtual ActionResult AddProductToCart(string productId)
        {
            var cart = ECommerceContext.Cart;
            if (cart == null )
            {
                cart = ECommerceContext.Client.CartService.CreateCart();
            }
            cart = ECommerceContext.Client.CartService.AddProductToCart(cart, productId, 1);
            ECommerceContext.Cart = cart;
            return Content(cart.Count.ToString());
        }

        /// <summary>
        /// Remove Product from Cart
        /// </summary>
        /// <param name="productId"></param>
        /// <returns></returns>
        public virtual ActionResult RemoveProductFromCart(string productId)
        {
            Log.Info("Removing product from cart: " + productId);
            var cart = ECommerceContext.Cart;
            if (cart == null)
            {
                cart = ECommerceContext.Client.CartService.CreateCart();
                return Content("0");
            }
            cart = ECommerceContext.Client.CartService.RemoveProductFromCart(cart, productId);
            ECommerceContext.Cart = cart;
            return Content(cart.Count.ToString());
        }
    }
}