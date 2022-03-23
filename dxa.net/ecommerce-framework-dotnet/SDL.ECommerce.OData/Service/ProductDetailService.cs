using Microsoft.OData.Client;
using Sdl.Web.Delivery.Service;
using SDL.ECommerce.Api.Service;

using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace SDL.ECommerce.OData
{
    /// <summary>
    /// E-Commerce Product Detail Service
    /// </summary>
    public class ProductDetailService : IProductDetailService
    {
        private ECommerceClient ecommerceClient;

        /// <summary>
        /// Constructor (only available internally)
        /// </summary>
        /// <param name="service"></param>
        internal ProductDetailService(ECommerceClient ecommerceClient)
        {
            this.ecommerceClient = ecommerceClient;
        }

        public IProduct GetDetail(string productId)
        {
            try
            {
                // TODO: Could this be issue in MT sitations???
                // System.InvalidOperationException: 'The context is already tracking a different entity with the same resource Uri.'
                //
                return this.ecommerceClient.ODataV4Service.Products.ByKey(productId).GetValue();
            }
            catch (DataServiceQueryException)
            {
                return null;
            }
        }

        public IProduct GetDetail(string productId, IDictionary<string, string> variantAttributes)
        {
            ODataV4ClientFunction func = new ODataV4ClientFunction("ProductVariant");
            func.AllowCaching = false;
            func.WithParam("productId", productId);
            if ( variantAttributes != null )
            {
                var variantAttributesStr = new StringBuilder();
                bool firstParam = true;
                foreach ( var attributeId in variantAttributes.Keys )
                {
                    if ( firstParam )
                    {
                        firstParam = false;
                    }
                    else
                    {
                        variantAttributesStr.Append("&");
                    }
                    variantAttributesStr.Append(attributeId + "=" + variantAttributes[attributeId]);
                }
                func.WithParam("variantAttributes", variantAttributesStr.ToString());
            }

            try
            {
                return Enumerable.FirstOrDefault<Product>(this.ecommerceClient.ODataV4Service.Execute<Product>(func));
            }
            catch (InvalidResourceException)
            {
                return null;
            }
            catch (DataServiceQueryException)
            {
                return null;
            }
        }
    }
}
