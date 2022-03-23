using SDL.ECommerce.Api.Service;

using System.Linq;

using SDL.ECommerce.Api.Model;
using Sdl.Web.Delivery.Service;

namespace SDL.ECommerce.OData
{
    public class EditService : IEditService
    {
        private ECommerceClient ecommerceClient;

        /// <summary>
        /// Constructor (only available internally)
        /// </summary>
        /// <param name="ecommerceClient"></param>
        internal EditService(ECommerceClient ecommerceClient)
        {
            this.ecommerceClient = ecommerceClient;
        }

        public IEditMenu GetInContextMenuItems(Query query)
        {
            ODataV4ClientFunction func = new ODataV4ClientFunction("GetInContextEditMenu");
            func.AllowCaching = true;

            // Category
            //
            if (query.Category != null)
            {
                func.WithParam("categoryId", query.Category.Id);
            }
            else if (query.CategoryId != null)
            {
                func.WithParam("categoryId", query.CategoryId);
            }

            // Search phrase
            //
            if (query.SearchPhrase != null)
            {
                func.WithParam("searchPhrase", query.SearchPhrase);
            }

            return Enumerable.FirstOrDefault<EditMenu>(this.ecommerceClient.ODataV4Service.Execute<EditMenu>(func));
        }
    }
}
