namespace SDL.ECommerce.Api.Model
{
    /// <summary>
    /// Markes a specific E-Commerce entity (facet, promotion etc) as editable. When entering XPM
    /// the provided edit URL is used to generate in-context edit controls.
    /// </summary>
    public interface IEditable
    {
        /// <summary>
        /// Returns edit URL to either custom DXA dialog or a wrapper around existing edit GUI in the E-Commerce system.
        /// </summary>
        /// <returns></returns>
        string EditUrl { get; }
    }
}
