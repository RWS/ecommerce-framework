using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace SDL.ECommerce.Ecl
{
    /// <summary>
    /// Cached Product. Is used to cache product a few times during the build of the lister view.
    /// This to avoid unncessary calls to get product details when getting thumbnails etc.
    /// </summary>
    public class CachedProduct
    {
        public int Requested { get; set; }
        public Product Product { get; set; }
        public bool HasFullData { get; set; }
    }
}
