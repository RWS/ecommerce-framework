using SDL.ECommerce.Api.Model;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace SDL.ECommerce.Rest.Model
{
    public class ProductVariantAttributeValueType : IProductVariantAttributeValueType
    {
        public string Id { get; set; }
        public bool Applicable { get; set; }
        public bool Selected { get; set; }
        public string Value { get; set; }

        bool IProductVariantAttributeValueType.IsAvailable
        {
            get
            {
                return Applicable;
            }
        }

        bool IProductVariantAttributeValueType.IsSelected
        {
            get
            {
                return Selected;
            }
        }
    }
}
