using Sdl.Web.Common.Logging;
using Sdl.Web.Common.Models;
using SDL.ECommerce.Api;
using SDL.ECommerce.DXA.Factories;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text.RegularExpressions;
using System.Web;
using System.Web.Mvc;

namespace SDL.ECommerce.DXA.Html
{
    /// <summary>
    /// HTML Helper Extensions
    /// </summary>
    public static class HtmlHelperExtensions
    {
        private static readonly Regex PRODUCT_URI_REGEX = new Regex("/(.+)/+(.)+-(?<productId>[^-]*)-product-file_tcm[^.]*.ecl", RegexOptions.Compiled);
        private static readonly Regex CATEGORY_URI_REGEX = new Regex("/(.+)/+(.)+-(?<categoryId>[^-]*)-category-file_tcm[^.]*.ecl", RegexOptions.Compiled);

        private static IECommerceLinkResolver _linkResolver;

        /// <summary>
        /// Extension to the standard DXA @Html.DxaRichText extension method that also resolves Commerce links.
        /// </summary>
        /// <param name="htmlHelper"></param>
        /// <param name="richText"></param>
        /// <returns></returns>
        public static MvcHtmlString CommerceRichText(this HtmlHelper htmlHelper, RichText richText)
        {
            var htmlString = Sdl.Web.Mvc.Html.HtmlHelperExtensions.DxaRichText(htmlHelper, richText);
            if (htmlString != null)
            {
                var xhtml = htmlString.ToHtmlString();
                xhtml = PRODUCT_URI_REGEX.Replace(xhtml, m => ResolveProductUrl(m.Groups["productId"].Value));
                xhtml = CATEGORY_URI_REGEX.Replace(xhtml, m => ResolveCategoryUrl(m.Groups["categoryId"].Value));
                htmlString = new MvcHtmlString(xhtml);
            }            
            return htmlString;
        }

        private static string ResolveProductUrl(string productId)
        {
            var product = ECommerceContext.Client.DetailService.GetDetail(productId);
            return LinkResolver.GetProductDetailLink(product);
        }

        private static string ResolveCategoryUrl(string categoryId)
        {
            var category = ECommerceContext.Client.CategoryService.GetCategoryById(categoryId);
            return LinkResolver.GetCategoryLink(category);
        }

        private static IECommerceLinkResolver LinkResolver
        {
            get
            {
                if (_linkResolver == null)
                {
                    _linkResolver = DependencyFactory.Current.Resolve<IECommerceLinkResolver>();
                }
                return _linkResolver;
            }
        }
    }
}