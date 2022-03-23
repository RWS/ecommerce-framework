using System.Collections.Generic;

namespace SDL.ECommerce.Api.Model
{
    /// <summary>
    /// Product Category interface
    /// </summary>
    public interface ICategory
    {
        /// <summary>
        /// Get category ID
        /// </summary>

        string Id { get; }

        /// <summary>
        /// Get name of the category. Should be localized to current language.
        /// </summary>               
        string Name { get; }

        /// <summary>
        /// Get parent category. Returns NULL if root category.
        /// </summary>
        ICategory Parent { get; }

        /// <summary>
        /// Get all sub-categories.
        /// </summary>
        IList<ICategory> Categories { get; }

        /// <summary>
        ///  Get the category's path name which can based on the name of the category.
        /// </summary>
        string PathName { get; }

        /// <summary>
        ///  Get the category's sanitized path name which can based on the name of the category.
        /// </summary>
        string SanitizedPathName { get; }
    }
}
