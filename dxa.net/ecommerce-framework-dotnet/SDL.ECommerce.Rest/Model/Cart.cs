using SDL.ECommerce.Api.Model;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace SDL.ECommerce.Rest.Model
{
    public class Cart : ICart
    {
        public string Id { get; set; }
        public string SessionId { get; set; }
        public int Count { get; set; }
        public ProductPrice TotalPrice { get; set; }
        public List<CartItem> Items { get; set; }
       
        IList<ICartItem> ICart.Items
        {
            get
            {
                if ( Items != null )
                {
                    return Items.Cast<ICartItem>().ToList();
                }
                else
                {
                    return Enumerable.Empty<ICartItem>().ToList();
                }
                
            }
        }

        IProductPrice ICart.TotalPrice
        {
            get
            {
                return TotalPrice;
            }
        }
    }
}
