using SDL.ECommerce.Api.Model;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace SDL.ECommerce.Rest.Model
{
    public class ProductAttributeValue : IProductAttributeValue
    {
        public string PresentationValue { get; set; }
        public string Value { get; set; }
    }
}
