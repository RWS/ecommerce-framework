using SDL.ECommerce.Api.Model;

namespace SDL.ECommerce.Api.Service
{
    /// <summary>
    /// E-Commerce Product Query Service
    /// </summary>
    public interface IProductQueryService
    {
        /// <summary>
        /// Submit the query to the query service
        /// </summary>
        /// <param name="query"></param>
        /// <returns></returns>
        IProductQueryResult Query(Query query);
    }
}
