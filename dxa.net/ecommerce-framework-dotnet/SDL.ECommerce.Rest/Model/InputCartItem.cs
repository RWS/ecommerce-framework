using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace SDL.ECommerce.Rest.Model
{
    public class InputCartItem
    {
        public string ItemId { get; set; }
        public string Type { get; set; } // For future use when adding promo codes etc to the cart
        public int Quantity { get; set; }
        public string Operation { get; set; }

    }
}
