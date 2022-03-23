using SDL.ECommerce.Api.Model;

namespace SDL.ECommerce.Api.Service
{
    /// <summary>
    /// E-Commerce Edit Service
    /// </summary>
    public interface IEditService
    {
        /// <summary>
        /// Get In-Context edit menu items for current query
        /// </summary>
        /// <param name="query"></param>
        /// <returns></returns>
        IEditMenu GetInContextMenuItems(Query query);
    }
}
