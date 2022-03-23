using System;
using System.Collections.Generic;
using Tridion.ExternalContentLibrary.V2;

namespace SDL.ECommerce.Ecl
{
    /// <summary>
    /// Selectable category ECL item.
    /// </summary>
    public class SelectableCategoryItem : SelectableItem
    {
   
        protected Category category;

        public SelectableCategoryItem(int publicationId, Category category ) : base(publicationId, category.CategoryId, category.Title)
        {
            this.category = category;
        }

        public override string Title
        {
            get {
                // Display the full path of the category as title
                //
                String categoryPath = "";
                Category currentCategory = this.category;
                while ( currentCategory != null )
                {
                    if (currentCategory.Title != null)
                    {
                        if (categoryPath.Length > 0)
                        {
                            categoryPath = "->" + categoryPath;
                        }
                        categoryPath = currentCategory.Title + categoryPath;
                    }
                    currentCategory = currentCategory.Parent;
                }
                return categoryPath;
            }
            set { throw new NotSupportedException(); }
        }

        public override string DisplayTypeId
        {
            get { return "category"; }
        }

        public override string GetDirectLinkToPublished(IList<ITemplateAttribute> attributes)
        {
            // Return a E-Com FW specific URI to the category. This can be used to resolve to
            // concrete site URLs to category pages (for example via an custom RTF processor).
            //
            return "ecom:category:" + this.category.CategoryId + ":uri";
        }

        public override string GetTemplateFragment(IList<ITemplateAttribute> attributes)
        {
            // TODO: What to output here??
            return category.Title;
       }

        protected override void GetMetadata(Dictionary<string, object> metadata)
        {
            // Doing nothing. No additional metadata is exposed for category items
        }

        protected override void BuildMetadataXmlSchema(ISchemaDefinition schema)
        {
            // Doing nothing. No additional metadata is exposed for category items
        }

        public override IEclUri ParentId
        {
            get
            {
                // return folder uri 
                return EclProvider.HostServices.CreateEclUri(
                    Id.PublicationId,
                    Id.MountPointId,
                    "Type_Categories",
                    "type",
                    EclItemTypes.Folder);
            }
        }

    }
}
