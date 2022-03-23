namespace SDL.ECommerce.DXA.Servants
{
    using System.Collections.Generic;

    using SDL.ECommerce.Api.Model;

    public interface IPathServant
    {
        IEnumerable<string> GetSearchPath(string url, ICategory category);

        IEnumerable<string> GetSearchPath(string productSeoId, IProduct product);
    }
}