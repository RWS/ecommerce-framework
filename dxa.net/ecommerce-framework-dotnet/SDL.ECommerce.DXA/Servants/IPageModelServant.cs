namespace SDL.ECommerce.DXA.Servants
{
    using System.Collections.Generic;

    using Sdl.Web.Common.Interfaces;
    using Sdl.Web.Common.Models;

    public interface IPageModelServant
    {
        PageModel ResolveTemplatePage(IEnumerable<string> urlSegments, IContentProvider contentProvider);

        void SetTemplatePage(PageModel model);

        void GetQueryContributions(PageModel templatePage, Api.Model.Query query);

        PageModel GetNotFoundPageModel(IContentProvider contentProvider);
    }
}