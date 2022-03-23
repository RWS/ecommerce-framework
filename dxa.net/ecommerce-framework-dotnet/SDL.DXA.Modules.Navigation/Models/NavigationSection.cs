using Sdl.Web.Common.Models;

using System.Collections.Generic;

namespace SDL.DXA.Modules.Navigation.Models
{
    /// <summary>
    /// Navigation Section
    /// </summary>
    [SemanticEntity(Vocab = CoreVocabulary, EntityName = "NavigationSection", Prefix = "e")]
    public class NavigationSection : EntityModel
    {
        // TODO: Have this configurable in metadata
        const int COLS = 4;

        [SemanticProperty("e:mainItem")]
        public NavigationItem MainItem { get; set; }

        [SemanticProperty("e:items")]
        public IList<NavigationItem> Items { get; set; }

        /// <summary>
        /// Get items in columns
        /// </summary>
        /// <returns></returns>
        public IList<IList<NavigationItem>> GetItemColumns()
        {
            var columns = new List<IList<NavigationItem>>();
            if (Items != null)
            {
                var column = new List<NavigationItem>();
                columns.Add(column);
                int i = 0;
                foreach (var item in Items)
                {
                    column.Add(item);
                    i++;
                    if (i % COLS == 0)
                    {
                        column = new List<NavigationItem>();
                        columns.Add(column);
                    }
                }
            }
            return columns;
        }
    }
}