using Sdl.Web.Common.Models;
using SDL.ECommerce.Api.Model;

using System.Collections.Generic;

namespace SDL.ECommerce.DXA.Models
{
    [SemanticEntity(Vocab = CoreVocabulary, EntityName = "BreadcrumbWidget", Prefix = "e")]
    public class BreadcrumbWidget : EntityModel
    {
        [SemanticProperty("e:category")]
        public ECommerceCategoryReference CategoryReference { get; set; }

        [SemanticProperty("e:product")]
        public ECommerceProductReference ProductReference { get; set; }

        [SemanticProperty(IgnoreMapping = true)]
        public IList<IBreadcrumb> Breadcrumbs { get; set; }

        [SemanticProperty(IgnoreMapping = true)]
        public int TotalItems { get; set; }
    }
}