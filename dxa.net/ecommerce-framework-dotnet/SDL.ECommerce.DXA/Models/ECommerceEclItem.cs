using Sdl.Web.Common.Models;
using SDL.ECommerce.Api.Model;

namespace SDL.ECommerce.DXA.Models
{
    /// <summary>
    /// E-Commerce ECL Item
    /// </summary>
    public class ECommerceEclItem : EclItem
    {
        /// <summary>
        /// External ID
        /// </summary>
        public string ExternalId
        {
            get
            {
                var id = this.GetEclExternalMetadataValue("Id") as string;
                if ( id == null )
                {
                    id = FileName;
                }
                return id;
            }
        }

        /// <summary>
        /// External Name
        /// </summary>
        public string ExternalName
        {
            get
            {
                return this.GetEclExternalMetadataValue("Name") as string;
            }
        }

        /// <summary>
        /// Concrete references to products & categories consumed by ECL views
        /// </summary>
        public IProduct Product { get; set; }
        public ICategory Category { get; set; }
    }
}