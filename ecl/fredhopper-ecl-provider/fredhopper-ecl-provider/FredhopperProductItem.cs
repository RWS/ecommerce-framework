using SDL.ECommerce.Ecl;
using System.Collections.Generic;
using System.Security;
using Tridion.ExternalContentLibrary.V2;

namespace SDL.Fredhopper.Ecl
{
    class FredhopperProductItem : ProductItem
    {
        public FredhopperProductItem(int publicationId, Category category, Product product) : base(publicationId, category, product) {}

        protected override void GetMetadata(Dictionary<string, object> metadata)
        {
            if (this.product is FredhopperProduct)
            {
                var fhProduct = (FredhopperProduct)this.product;
                metadata.Add("Description", SecurityElement.Escape(fhProduct.Description));
                metadata.Add("Price", fhProduct.Price);
                foreach (var attribute in fhProduct.AdditionalAttributes)
                {
                    var name = getSchemaAttributeName(attribute.Key) + "__xml"; // Indicate that the value is a xml string

                    object value = null;
                    if (attribute.Value.GetType() == typeof(ProductAttributeValue))
                    {
                        var attributeValue = (ProductAttributeValue)attribute.Value;
                        value = attributeValue.ToXml(); ;
                    }
                    else if (attribute.Value.GetType() == typeof(List<ProductAttributeValue>))
                    {
                        var attributeList = (List<ProductAttributeValue>)attribute.Value;
                        var xmlList = new List<string>();
                        attributeList.ForEach(val => xmlList.Add(val.ToXml()));
                        value = xmlList;
                    }

                    if (value != null)
                    {
                        metadata.Add(name, value);
                    }
                }
            }
        }

        protected override void BuildMetadataXmlSchema(ISchemaDefinition schema)
        {
            schema.Fields.Add(EclProvider.HostServices.CreateMultiLineTextFieldDefinition("Description", "Description", 0, 1, 7));
            schema.Fields.Add(EclProvider.HostServices.CreateSingleLineTextFieldDefinition("Price", "Price", 0, 1));
           

            var attributeValueField = EclProvider.HostServices.CreateFieldGroupDefinition("ProductAttributeValue", "Attribute Value", 0, null);
            var attributeValueFields = new List<IFieldDefinition>
            {
                EclProvider.HostServices.CreateSingleLineTextFieldDefinition("Value", "Value", 0, 1),
                EclProvider.HostServices.CreateSingleLineTextFieldDefinition("PresentationValue", "Presentation Value", 0, 1)
            };

            if (this.product is FredhopperProduct)
            {
                var fhProduct = (FredhopperProduct)this.product;
                foreach (var attribute in fhProduct.AdditionalAttributes)
                {
                    var name = getSchemaAttributeName(attribute.Key);
                    if (attribute.Value.GetType() == typeof(ProductAttributeValue))
                    {
                        var field = EclProvider.HostServices.CreateFieldGroupDefinition(name, name, 0, 1);
                        attributeValueFields.ForEach(f => field.Fields.Add(f));
                        schema.Fields.Add(field);
                    }
                    else if (attribute.Value.GetType() == typeof(List<ProductAttributeValue>))
                    {
                        var field = EclProvider.HostServices.CreateFieldGroupDefinition(name, name, 0, null);
                        attributeValueFields.ForEach(f => field.Fields.Add(f));
                        schema.Fields.Add(field);
                    }
                }
            }
        }

        private string getSchemaAttributeName(string attributeName)
        {
            return attributeName.Substring(0, 1).ToUpper() + attributeName.Substring(1);
        }
    }
}
