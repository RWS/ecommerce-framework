namespace SDL.ECommerce.DXA.Servants
{
    using System.Collections.Generic;
    using System.Web;

    using Sdl.Web.Common;
    using Sdl.Web.Common.Interfaces;
    using Sdl.Web.Common.Logging;
    using Sdl.Web.Common.Models;
    using Sdl.Web.Mvc.Configuration;

    using SDL.ECommerce.Api.Model;

    public class PageModelServant : IPageModelServant
    {
        public PageModel ResolveTemplatePage(IEnumerable<string> urlSegments, IContentProvider contentProvider)
        {
            PageModel model = null;

            foreach (var urlSegment in urlSegments)
            {
                try
                {
                    Log.Info("Trying to find page template: " + urlSegment);

                    model = contentProvider.GetPageModel(urlSegment, WebRequestContext.Localization);
                }
                catch (DxaItemNotFoundException) { }

                if (model != null)
                {
                    break;
                }
            }

            return model;
        }

        public void SetTemplatePage(PageModel model)
        {
            if (model != null)
            {
                WebRequestContext.PageModel = model;
            }
        }

        public void GetQueryContributions(PageModel templatePage, Api.Model.Query query)
        {
            foreach (var region in templatePage.Regions)
            {
                if (!region.Name.Equals("Header") && !region.Name.Equals("Footer"))
                {
                    foreach (EntityModel entity in region.Entities)
                    {
                        if (entity is IQueryContributor)
                        {
                            ((IQueryContributor)entity).ContributeToQuery(query);
                        }
                    }
                }
            }
        }

        public PageModel GetNotFoundPageModel(IContentProvider contentProvider)
        {
            // TODO: Have the possiblity to have a E-Commerce specific 404 page for categories and products
            //
            string notFoundPageUrl = ECommerceContext.LocalizePath("/error-404");

            PageModel pageModel;
            try
            {
                pageModel = contentProvider.GetPageModel(notFoundPageUrl, WebRequestContext.Localization);
            }
            catch (DxaItemNotFoundException ex)
            {
                Log.Error(ex);
                throw new HttpException(404, ex.Message);
            }

            return pageModel;
        }
    }
}