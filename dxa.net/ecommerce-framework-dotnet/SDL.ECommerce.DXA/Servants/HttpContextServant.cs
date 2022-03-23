using System.Collections.Generic;
using System.Web;
using System.Linq;

using SDL.ECommerce.Api;

namespace SDL.ECommerce.DXA.Servants
{
    public class HttpContextServant : IHttpContextServant
    {
        private const string STARTINDEX_KEY = "startIndex";

        private const string QUERY_KEY = "q";

        private readonly string[] _excludedParameters = { QUERY_KEY, STARTINDEX_KEY, ECommerceContext.ENVIRONMENT_REQUEST_PARAM };

        public IList<FacetParameter> GetFacetParametersFromRequest(HttpContextBase httpContext)
        {
            var facetParameters = new List<FacetParameter>();

            var queryParams = httpContext.Request.QueryString;

            foreach (var key in queryParams.AllKeys.Except(_excludedParameters))
            {
                // TODO:  Use a global facet map here instead to validate against
                var paramValue = queryParams[key];
                facetParameters.Add(new FacetParameter(key, paramValue));
            }

            return facetParameters;
        }

        public int GetStartIndex(HttpContextBase httpContext)
        {
            var startIndex = 0;

            var startIndexParameter = httpContext.Request.QueryString[STARTINDEX_KEY];

            if (startIndexParameter != null)
            {
                int.TryParse(startIndexParameter, out startIndex);
            }

            return startIndex;
        }
    }
}