using SDL.ECommerce.Ecl;
using Tridion.ExternalContentLibrary.V2;

namespace SDL.Fredhopper.Ecl
{
    class FredhopperMountpoint : Mountpoint
    {

        public FredhopperMountpoint(IEclSession session) : base(session)
        {
        }

        public override bool CanSearch(int publicationId)
        {
            return true;
        }

        protected override ProductItem CreateProductItem(int publicationId, Category parentCategory, Product product)
        {
            return new FredhopperProductItem(publicationId, parentCategory, product);
        }
    }
}
