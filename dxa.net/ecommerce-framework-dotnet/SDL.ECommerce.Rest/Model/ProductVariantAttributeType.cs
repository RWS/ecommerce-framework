using SDL.ECommerce.Api.Model;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace SDL.ECommerce.Rest.Model
{
    public class ProductVariantAttributeType : IProductVariantAttributeType
    {
        public string Id { get; set; }
        public string Name { get; set; }
        public List<ProductVariantAttributeValueType> Values { get; set; }

        IList<IProductVariantAttributeValueType> IProductVariantAttributeType.Values
        {
            get
            {
                return Values.Cast<IProductVariantAttributeValueType>().ToList();
            }
        }
    }
}
