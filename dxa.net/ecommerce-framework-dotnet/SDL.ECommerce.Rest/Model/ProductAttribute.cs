using SDL.ECommerce.Api.Model;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace SDL.ECommerce.Rest.Model
{
    public class ProductAttribute : IProductAttribute
    {
        public string Id { get; set; }
        public string Name { get; set; }
        public bool IsSingleValue { get; set; }
        public List<ProductAttributeValue> Values { get; set; }

        public IProductAttributeValue Value
        {
            get
            {
                if ( IsSingleValue && Values != null && Values.Count > 0 )
                {
                    return Values[0];
                }
                return null;
            }
        }

        IList<IProductAttributeValue> IProductAttribute.Values 
        {
            get { return Values?.Cast<IProductAttributeValue>().ToList(); }
        }


    }
}
