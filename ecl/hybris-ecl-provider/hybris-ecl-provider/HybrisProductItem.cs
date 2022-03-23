using SDL.ECommerce.Ecl;
using System.Collections.Generic;
using System.Security;
using Tridion.ExternalContentLibrary.V2;

namespace SDL.Hybris.Ecl
{
    class HybrisProductItem : ProductItem
    {
        public HybrisProductItem(int publicationId, Category category, Product product) : base(publicationId, category, product) {}

        protected override void GetMetadata(Dictionary<string, object> metadata)
        {
            var hybrisProduct = (HybrisProduct) this.product;
            metadata.Add("Description", SecurityElement.Escape(hybrisProduct.Description));
            metadata.Add("Manufacturer", SecurityElement.Escape(hybrisProduct.Manufacturer));
            metadata.Add("Purchasable", hybrisProduct.Purchasable.ToString());
            metadata.Add("Price", hybrisProduct.Price);
            metadata.Add("StockLevel", hybrisProduct.StockLevel);
            metadata.Add("AverageRating", hybrisProduct.AverageRating);
        }

        protected override void BuildMetadataXmlSchema(ISchemaDefinition schema)
        {
            schema.Fields.Add(EclProvider.HostServices.CreateMultiLineTextFieldDefinition("Description", "Description", 0, 1, 7));
            schema.Fields.Add(EclProvider.HostServices.CreateSingleLineTextFieldDefinition("Manufacturer", "Manufacturer", 0, 1));
            schema.Fields.Add(EclProvider.HostServices.CreateSingleLineTextFieldDefinition("Purchasable", "Purchasable", 0, 1));
            schema.Fields.Add(EclProvider.HostServices.CreateSingleLineTextFieldDefinition("Price", "Price", 0, 1));
            schema.Fields.Add(EclProvider.HostServices.CreateSingleLineTextFieldDefinition("StockLevel", "StockLevel", 0, 1));
            schema.Fields.Add(EclProvider.HostServices.CreateSingleLineTextFieldDefinition("AverageRating", "AverageRating", 0, 1));
        }
    }
}
