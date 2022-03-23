namespace SDL.ECommerce.DXA.Servants
{
    using System.Collections.Generic;
    using System.Web;

    using SDL.ECommerce.Api;

    public interface IHttpContextServant
    {
        IList<FacetParameter> GetFacetParametersFromRequest(HttpContextBase httpContext);

        int GetStartIndex(HttpContextBase httpContext);
    }
}