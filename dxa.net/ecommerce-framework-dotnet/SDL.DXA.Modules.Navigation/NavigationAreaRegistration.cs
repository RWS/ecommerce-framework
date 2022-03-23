using Sdl.Web.Common.Models;
using Sdl.Web.Mvc.Configuration;
using SDL.DXA.Modules.Navigation.Models;

namespace SDL.DXA.Modules.Navigation
{
    public class NavigationAreaRegistration : BaseAreaRegistration
    {
        public override string AreaName
        {
            get
            {
                return "Navigation";
            }
        }

        protected override void RegisterAllViewModels()
        {
            // Entity views
            //
            RegisterViewModel("NavigationSection", typeof(NavigationSection));

            // Region views
            //
            RegisterViewModel("MegaNavigation", typeof(RegionModel));
        }
    }
}