using Sdl.Web.Common.Models;
using SDL.ECommerce.Api.Model;

using System.Collections.Generic;
using System.Linq;

namespace SDL.ECommerce.DXA.Models
{
    [SemanticEntity(Vocab = CoreVocabulary, EntityName = "ProductListerWidget", Prefix = "e")]
    public class ProductListerWidget : EntityModel, IQueryContributor
    {
        [SemanticProperty("e:categories")]
        public List<ECommerceCategoryReference> CategoryReferences { get; set; }

        [SemanticProperty("e:viewSize")]
        public int? ViewSize { get; set; }

        [SemanticProperty("e:viewType")]
        public string ViewType { get; set; }

        [SemanticProperty("e:filterAttributes")]
        public List<ECommerceFilterAttribute> FilterAttributes { get; set; }

        [SemanticProperty("e:fallbackContent")]
        public EntityModel FallbackContent { get; set; }

        [SemanticProperty("e:contextData")]
        public List<NameValuePair> ContextData { get; set; }

        [SemanticProperty(IgnoreMapping = true)]
        public IList<IProduct> Items { get; set; }
    
        [SemanticProperty(IgnoreMapping = true)]
        public ListerNavigationData NavigationData { get; set; }

        public void ContributeToQuery(Api.Model.Query query)
        {
            if (ViewSize != null)
            {
                query.ViewSize = ViewSize;
            }
            if (FilterAttributes != null)
            {
                foreach (var filterAttribute in FilterAttributes)
                {
                    query.Facets.Add(new Api.FacetParameter(filterAttribute.Name + "_hidden", filterAttribute.Value));
                }
            } 
            if (ContextData != null)
            {
                query.ContextData = ContextData.ToDictionary(c => c.Name, c => c.Value);
            }
        }
    }
}
