using SDL.ECommerce.Api.Model;

using System.Collections.Generic;
using System.Linq;

namespace SDL.ECommerce.OData
{
    public partial class Cart : ICart
    {
        int ICart.Count
        {
            get
            {
                return (int) this.Count;
            }
        }

        IList<ICartItem> ICart.Items
        {
            get
            {
                return this.Items.Cast<ICartItem>().ToList();
            }
        }

        IProductPrice ICart.TotalPrice
        {
            get
            {
                return this.TotalPrice;
            }
        }
    }
}
