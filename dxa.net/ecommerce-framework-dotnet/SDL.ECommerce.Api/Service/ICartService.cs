using SDL.ECommerce.Api.Model;

namespace SDL.ECommerce.Api.Service
{
    public interface ICartService
    {
        /// <summary>
        /// Create new cart
        /// </summary>
        /// <returns></returns>
        ICart CreateCart();

        /// <summary>
        /// Add product to cart
        /// </summary>
        /// <param name="cart"></param>
        /// <param name="productId"></param>
        /// <param name="quantity"></param>
        /// <returns></returns>
        ICart AddProductToCart(ICart cart, string productId, int quantity);

        /// <summary>
        /// Remove product from cart
        /// </summary>
        /// <param name="cart"
        /// <param name="productId"></param>
        /// <returns></returns>
        ICart RemoveProductFromCart(ICart cart, string productId);
    }
}
