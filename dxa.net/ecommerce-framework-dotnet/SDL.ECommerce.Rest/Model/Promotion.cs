using SDL.ECommerce.Api.Model;

using System.Collections.Generic;
using System.Linq;

namespace SDL.ECommerce.Rest.Model
{
       
    public class Promotion
    {
        public string Id { get; set; }
        public string Name { get; set; }
        public string Title { get; set; }
        public string EditUrl { get; set; }
        public ProductsPromotion ProductsPromotion { get; set; }
        public ContentPromotion ContentPromotion { get; set; }

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

    public class ProductsPromotion : AbstractPromotion, IProductsPromotion
    {
        public List<Product> Products { get; set; }

        List<IProduct> IProductsPromotion.Products
        {
            get
            {
                return this.Products?.Cast<IProduct>().ToList() ?? Enumerable.Empty<IProduct>().ToList();
            }
        }
    }

    public class ContentPromotion : AbstractPromotion, IContentPromotion
    {
        public string Text { get; set; }
        public string ImageUrl { get; set; }
        public List<ContentArea> ContentAreas { get; set; }
        public Location Location { get; set; }
        ILocation IContentPromotion.Location
        {
            get
            {
                return Location;
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
