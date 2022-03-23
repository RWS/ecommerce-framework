using System;
using System.Collections.Generic;
using SDL.ECommerce.Api.Model;
using System.Linq;

namespace SDL.ECommerce.OData
{
    public partial class ProductAttribute : IProductAttribute
    {

        public IProductAttributeValue Value
        {
            get
            {
                if ( (bool) IsSingleValue && Values != null && Values.Count > 0 )
                {
                    return Values[0];
                }
                return null;
            }
        }

        bool IProductAttribute.IsSingleValue
        {
            get
            {
                return (bool) this.IsSingleValue;
            }
        }

        IList<IProductAttributeValue> IProductAttribute.Values
        {
            get
            {
                return Values?.Cast<IProductAttributeValue>().ToList();
            }
        }
    }
}
