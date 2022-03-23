using System.Collections.Generic;

namespace SDL.ECommerce.Api.Model
{
    public interface ICart
    {
        string Id { get; }
        string SessionId { get; }
        int Count { get; }
        IProductPrice TotalPrice { get; }
        IList<ICartItem> Items { get; }
    }
}
