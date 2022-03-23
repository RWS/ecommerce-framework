using SDL.ECommerce.Api.Model;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace SDL.ECommerce.Rest.Model
{
    public class EditMenu : IEditMenu
    {
        public string Title { get; set; }
        public List<MenuItem> MenuItems { get; set; }

        IList<IMenuItem> IEditMenu.MenuItems
        {
            get
            {
                return MenuItems?.Cast<IMenuItem>().ToList() ?? Enumerable.Empty<IMenuItem>().ToList();
            }
        }
    }

}
