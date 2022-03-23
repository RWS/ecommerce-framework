using System;
using System.Collections.Generic;

using SDL.ECommerce.Api.Model;
using Newtonsoft.Json;
using System.Linq;

namespace SDL.ECommerce.Rest.Model
{
    // TODO: Is this needed????

    class ProductSummary : IProduct
    {

        public string Id { get; set; }

        public string MasterId { get; set; }

        public string VariantId { get; set; }

        public string Name { get; set; }

        [JsonProperty(ItemConverterType = typeof(ProductPrice))]
        public IProductPrice Price { get; set; }
          
        public string ThumbnailUrl { get; set; }
        public List<ProductAttribute> Attributes { get; set; }

        IList<IProductAttribute> IProduct.Attributes
        {
            get
            {
                return Attributes?.Cast<IProductAttribute>().ToList();
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
