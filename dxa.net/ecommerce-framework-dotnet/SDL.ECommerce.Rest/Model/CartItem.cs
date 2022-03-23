using SDL.ECommerce.Api.Model;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace SDL.ECommerce.Rest.Model
{
    public class CartItem : ICartItem
    {
        public Product Product { get; set; }
        public ProductPrice Price { get; set; }
        public int Quantity { get; set; }

        IProductPrice ICartItem.Price
        {
            get
            {
                return Price;
            }
        }

        IProduct ICartItem.Product
        {
            get
            {
                return Product;
            }
        }

    }
}
