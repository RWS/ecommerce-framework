using System.Collections.Generic;

namespace SDL.ECommerce.Api.Model
{
    public interface IEditMenu
    {
        string Title { get; }
        IList<IMenuItem> MenuItems { get; }
    }
}