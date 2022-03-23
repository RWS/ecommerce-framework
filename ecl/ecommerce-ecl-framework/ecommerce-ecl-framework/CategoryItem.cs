using System;
using Tridion.ExternalContentLibrary.V2;

namespace SDL.ECommerce.Ecl
{
    public class CategoryItem : IContentLibraryListItem, IContentLibraryItem
    {
        protected readonly IEclUri id;
        protected readonly Category category;

        public CategoryItem(int publicationId, Category category)
        {
            this.category = category;
            this.id = EclProvider.HostServices.CreateEclUri(publicationId, EclProvider.MountPointId, 
                category.CategoryId != null ? category.CategoryId : "0", DisplayTypeId, EclItemTypes.Folder);
        }

        public bool CanGetUploadMultimediaItemsUrl
        {
            get { return false; }
        }

        public bool CanSearch
        {
            get { return false; }
        }

        public string DisplayTypeId
        {
            get { return "category"; }
        }

        public string IconIdentifier
        {
            get { return null; }
        }

        public IEclUri Id
        {
            get { return id; }
        }

        public bool IsThumbnailAvailable
        {
            get { return false; }
        }

        public DateTime? Modified
        {
            get { return null; }
        }

        public string ThumbnailETag
        {
            get { return null; }
        }

        public string Title
        {
            get { return this.category.Title; }
            set { throw new NotSupportedException(); }
        }

        // allow override of dispatch
        public virtual string Dispatch(string command, string payloadVersion, string payload, out string responseVersion)
        {
            throw new NotSupportedException();
        }

        public IEclUri ParentId
        {
            get
            {
                if (category.Parent == null)
                {
                    // Return 'Products' type folder
                    //
                    return EclProvider.HostServices.CreateEclUri(Id.PublicationId, Id.MountPointId, "Type_Products", "category", EclItemTypes.Folder);
                }
                else
                {
                    // Return parent folder
                    //
                    return EclProvider.HostServices.CreateEclUri(
                        Id.PublicationId,
                        Id.MountPointId,
                        category.Parent.CategoryId != null ? category.Parent.CategoryId : "0", 
                        "category",
                        EclItemTypes.Folder);
                }
            }
        }

        public string ModifiedBy
        {
            get { return CreatedBy; }
        }

        public string CreatedBy
        {
            get { return null; }
        }

        public DateTime? Created
        {
            get { return null; }
        }

        public string MetadataXml
        {
            get { return null; } 
            set { throw new NotSupportedException(); }
        }

        public ISchemaDefinition MetadataXmlSchema
        {
            get
            {
               return null;
            }
        }

        public bool CanUpdateTitle
        {
            get { return false; }
        }

        public bool CanGetViewItemUrl
        {
            get { return true; }
        }

        public bool CanUpdateMetadataXml
        {
            get { return false; }
        }

        public IContentLibraryItem Save(bool readback)
        {
            // as saving isn't supported, the result of saving is always the item itself
            return readback ? this : null;
        }
    }
}
