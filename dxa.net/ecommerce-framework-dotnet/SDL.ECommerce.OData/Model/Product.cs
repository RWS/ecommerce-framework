using System;
using System.Collections.Generic;
using System.Linq;
using SDL.ECommerce.Api.Model;

namespace SDL.ECommerce.OData
{
    public partial class Product : IProduct
    {
        string IProduct.Id
        {
            get
            {
                return this.Id;
            }
        }

        string IProduct.Name
        {
            get
            {
                return this.Name;
            }
        }

        string IProduct.Description
        {
            get
            {
                return this.Description;
            }
        }

        IProductPrice IProduct.Price
        {
            get
            {
                return this.Price;
            }
        }

        string IProduct.PrimaryImageUrl
        {
            get
            {
                return this.PrimaryImageUrl;
            }
        }

        string IProduct.ThumbnailUrl
        {
            get
            {
                return this.ThumbnailUrl;
            }
        }

        IList<IProductAttribute> IProduct.Attributes
        {
            get
            {
                return this.Attributes.Cast<IProductAttribute>().ToList();
            }
        }

        IList<ICategory> IProduct.Categories
        {
            get
            {
                return this.Categories.Cast<ICategory>().ToList();
            }
        }

        IList<IBreadcrumb> IProduct.Breadcrumbs
        {
            get
            {
                return this.Breadcrumbs.Cast<IBreadcrumb>().ToList();
            }
        }

        IList<IPromotion> IProduct.Promotions
        {
            get
            {
                return this.Promotions.Select(promo => promo.ToConcretePromotion()).ToList();
            }
        }

        IList<IProductVariant> IProduct.Variants
        {
            get
            {
                return this.Variants.Cast<IProductVariant>().ToList();
            }
        }

        IList<IProductAttribute> IProduct.VariantAttributes
        {
            get
            {
                return this.VariantAttributes.Cast<IProductAttribute>().ToList();
            }
        }

        IList<IProductVariantAttributeType> IProduct.VariantAttributeTypes
        {
            get
            {
                return this.VariantAttributeTypes.Cast<IProductVariantAttributeType>().ToList();
            }
        }

        VariantLinkType IProduct.VariantLinkType
        {
            get
            {
                return (VariantLinkType) Enum.Parse(typeof(VariantLinkType), this.VariantLinkType);
            }
        }
    }
}
