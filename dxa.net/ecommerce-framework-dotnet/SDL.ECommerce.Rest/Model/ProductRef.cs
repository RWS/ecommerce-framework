using SDL.ECommerce.Api;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace SDL.ECommerce.Rest.Model
{
    public class ProductRef : IProductRef
    {
        public string Id { get; set; }
        public string Name { get; set; }
    }
}
