using System.Collections.Generic;
using System.IO;
using System.Net;
using Tridion.ExternalContentLibrary.V2;

namespace SDL.ECommerce.Ecl
{
    /// <summary>
    /// Base class for product ECL items.
    /// </summary>
    public abstract class ProductItem : SelectableItem
    {
        protected Category category;
        protected Product product;

        /// <summary>
        /// Construct a new product item
        /// </summary>
        /// <param name="publicationId"></param>
        /// <param name="category"></param>
        /// <param name="product"></param>
        public ProductItem(int publicationId, Category category, Product product) : base(publicationId, product.Id, product.Name)
        {
            this.category = category;
            this.product = product;          
        }

        public override string DisplayTypeId
        {
            get { return "product"; }
        }

        public override bool IsThumbnailAvailable
        {
            get { return this.product.Thumbnail != null; }
        }

        /// <summary>
        /// Get ECL content. By default this means the product thumbnail.
        /// </summary>
        /// <param name="attributes"></param>
        /// <returns></returns>
        public override IContentResult GetContent(IList<ITemplateAttribute> attributes)
        {
            if ( this.product.Thumbnail != null)
            {
                using (WebClient webClient = new WebClient())
                {
                    Stream stream = new MemoryStream(webClient.DownloadData(this.product.Thumbnail.Url));
                    stream.Position = 0;
                    return EclProvider.HostServices.CreateContentResult(stream, stream.Length, this.product.Thumbnail.Mime);
                }
            }
            /*
             * TODO: Add no image here
             * else
            {
                using (Stream bitmapStream = System.IO.File.OpenRead(Path.Combine(Provider.AddInFolder, "themes\\NoImage.png")))
                {
                    MemoryStream memStream = new MemoryStream();
                    memStream.SetLength(bitmapStream.Length);
                    bitmapStream.Read(memStream.GetBuffer(), 0, (int)bitmapStream.Length);
                    return Provider.HostServices.CreateContentResult(memStream, bitmapStream.Length, "image/png");
                }
            }
             */

            // TODO: We can not return NULL here....

            return null;           
        }

        /// <summary>
        /// Direct link to the E-Commerce entity is normally not possible. So the best match is to use
        /// the URL of the product thumbnail.
        /// </summary>
        /// <param name="attributes"></param>
        /// <returns></returns>
        public override string GetDirectLinkToPublished(IList<ITemplateAttribute> attributes)
        {
            // Return a E-Com FW specific URI to the product. This can be used to resolve to
            // concrete site URLs to product detail pages (for example via an custom RTF processor).
            //
            return "ecom:product:" + this.product.Id + ":uri";
        }

        /// <summary>
        /// Get MIME type. Default is to return the MIME type of the product thumbnail.
        /// </summary>
        public override string MimeType
        {
            get 
            { 
                if ( this.product.Thumbnail != null )
                {
                    return this.product.Thumbnail.Mime;
                }
                return null; 
            }
        }

        /// <summary>
        /// Parent ID property
        /// </summary>
        public override IEclUri ParentId
        {
            get
            {
                var category = GetMainCategory();
                if ( category == null)
                {
                    return null;
                }

                // return folder uri 
                return EclProvider.HostServices.CreateEclUri(
                    Id.PublicationId,
                    Id.MountPointId,
                    category.CategoryId,
                    "category",
                    EclItemTypes.Folder);
            }
        }

        protected Category GetMainCategory()
        {    
            if (category != null)
            {
                return category;
            }
            else if (product.Categories != null && product.Categories.Count > 0)
            {
                return product.Categories[0];
            }
            return null;           
        }

    }
}
