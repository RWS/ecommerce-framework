using System;
using System.Collections.Generic;

using SDL.ECommerce.Api.Model;
using System.Linq;

namespace SDL.ECommerce.OData
{
    public partial class ProductSummary : IProduct
    {
        IProductPrice IProduct.Price
        {
            get
            {
                return this.Price;
            }
        }

        IList<IProductAttribute> IProduct.Attributes
        {
            get
            {
                return this.Attributes.Cast<IProductAttribute>().ToList();
            }
        }

        /***** METHODS NOT AVAILABLE FOR PRODUCT SUMMARY ONLY IN DETAIL PRODUCT INFO ******/

        public string Description
        {
            get
            {
                throw new NotImplementedException();
            }
        }

        public string PrimaryImageUrl
        {
            get
            {
                throw new NotImplementedException();
            }
        }

        public IList<ICategory> Categories
        {
            get
            {
                throw new NotImplementedException();
            }
        }

        public IList<IBreadcrumb> Breadcrumbs
        {
            get
            {
                throw new NotImplementedException();
            }
        }

        public IList<IPromotion> Promotions
        {
            get
            {
                throw new NotImplementedException();
            }
        }

        public IList<IProductVariant> Variants
        {
            get
            {
                throw new NotImplementedException();
            }
        }

        public IList<IProductAttribute> VariantAttributes
        {
            get
            {
                throw new NotImplementedException();
            }
        }

        public IList<IProductVariantAttributeType> VariantAttributeTypes
        {
            get
            {
                throw new NotImplementedException();
            }
        }

        public VariantLinkType VariantLinkType
        {
            get
            {
                throw new NotImplementedException();
            }
        }
    }
}
