using SDL.ECommerce.Api.Model;

using System.Collections.Generic;
using System.Linq;

namespace SDL.ECommerce.OData
{
    public partial class Promotion
    {
        public IPromotion ToConcretePromotion()
        {
            if ( this.ProductsPromotion != null )
            {
                this.ProductsPromotion.PromotionBase = this;
                return this.ProductsPromotion;
            }
            else if ( this.ContentPromotion != null && this.ContentPromotion.ContentAreas != null )
            {
                this.ContentPromotion.PromotionBase = this;
                return new ImageMapPromotion(this.ContentPromotion);
            }
            else
            {
                this.ContentPromotion.PromotionBase = this;
                return this.ContentPromotion;
            }
        }
    }

    public abstract class AbstractPromotion : IPromotion
    {
        internal Promotion PromotionBase { get; set; }
        public string Id
        {
            get
            {
                return PromotionBase.Id;
            }
        }
        public string Name
        {
            get
            {
                return PromotionBase.Name;
            }
        }

        public string Title
        {
            get
            {
                return PromotionBase.Title;
            }
        }

        public string EditUrl
        {
            get
            {
                return PromotionBase.EditUrl;
            }
        }
    }

    public partial class ProductsPromotion : AbstractPromotion, IProductsPromotion
    {
        List<IProduct> IProductsPromotion.Products
        {
            get
            {
                return this.Products.Cast<IProduct>().ToList();
            }
        }
    }

    public partial class ContentPromotion : AbstractPromotion, IContentPromotion
    {
        ILocation IContentPromotion.Location
        {
            get
            {
                return this.Location;
            }
        }
    }

    public class ImageMapPromotion : IImageMapPromotion
    {
        internal ImageMapPromotion(ContentPromotion contentPromotion)
        {
            ContentPromotionBase = contentPromotion;
        }

        internal ContentPromotion ContentPromotionBase { get; set; }

        public string Id
        {
            get
            {
                return ContentPromotionBase.Id;
            }
        }

        public string ImageUrl
        {
            get
            {
                return ContentPromotionBase.ImageUrl;
            }
        }

        public ILocation Location
        {
            get
            {
                return ContentPromotionBase.Location;
            }
        }

        public string Name
        {
            get
            {
                return ContentPromotionBase.Name;
            }
        }

        public string Text
        {
            get
            {
                return ContentPromotionBase.Text;
            }
        }

        public string Title
        {
            get
            {
                return ContentPromotionBase.Title;
            }
        }

        IList<IContentArea> IImageMapPromotion.ContentAreas
        {
            get
            {
                return ContentPromotionBase.ContentAreas.Cast<IContentArea>().ToList();
            }
        }

        public string EditUrl
        {
            get
            {
                return ContentPromotionBase.EditUrl;
            }
        }
    }


}
