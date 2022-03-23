using SDL.ECommerce.Api;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;

namespace SDL.ECommerce.DXA.Example.Views
{
    /// <summary>
    /// Simple singleton to access the DXA link resolver
    /// </summary>
    public sealed class LinkResolver
    {
        private static readonly IECommerceLinkResolver linkResolver = new DXALinkResolver();

        public static IECommerceLinkResolver Instance
        {
            get
            {
                return linkResolver;
            }
        }
    }
}