using System;
using System.Collections;
using System.Collections.Generic;
using System.Security;
using System.Text;
using Tridion.ExternalContentLibrary.V2;

namespace SDL.ECommerce.Ecl
{
    /// <summary>
    /// Base class for all selectable items
    /// </summary>
    public abstract class SelectableItem : IContentLibraryMultimediaItem 
    {
        protected readonly IEclUri id;
        protected string externalId;
        protected string title;

        protected SelectableItem(int publicationId, string externalId, string title)
        {
            this.id = EclProvider.HostServices.CreateEclUri(publicationId, EclProvider.MountPointId,
                     externalId, DisplayTypeId, EclItemTypes.File);
            this.externalId = externalId;
            this.title = title;
        }


        public virtual bool CanGetUploadMultimediaItemsUrl
        {
            get { return false; }
        }

        public bool CanSearch
        {
            get { return false; }
        }

        public virtual string IconIdentifier
        {
            get { return null; }
        }

        public IEclUri Id
        {
            get { return id; }
        }

        public virtual string Title
        {
            get { return this.title; }
            set { throw new NotSupportedException(); }
        }

        public virtual bool IsThumbnailAvailable
        {
            get { return false; } 
        }

        public virtual DateTime? Modified
        {
            get { return null; }
        }

        public virtual string ThumbnailETag
        {
            get { return null; }
        }

        public virtual string Filename
        {
            get { return this.externalId; }
        }

        public virtual string GetTemplateFragment(IList<ITemplateAttribute> attributes)
        {
            return "";
        }

        public virtual int? Height
        {
            get { return null; }
        }

        public virtual string MimeType
        {
            get
            {
                return null;
            }
        }

        public virtual int? Width
        {
            get { return null; }
        }

        public virtual bool CanGetViewItemUrl
        {
            get { return true; }
        }

        public virtual bool CanUpdateMetadataXml
        {
            get { return false; }
        }

        public virtual bool CanUpdateTitle
        {
            get { return false; }
        }

        public virtual DateTime? Created
        {
            get { return null; }
        }

        public virtual string CreatedBy
        {
            get { return null; }
        }

        /// <summary>
        /// Get content of the ECL item.  Should be overriden by subclass when for example outputting a product image.
        /// </summary>
        /// <param name="attributes"></param>
        /// <returns></returns>
        public virtual IContentResult GetContent(IList<ITemplateAttribute> attributes)
        {
            return null;
        }

        /// <summary>
        /// Metadata XML property
        /// </summary>
        public string MetadataXml
        {
            get
            {
                Dictionary<string, object> metadata = new Dictionary<string, object>();
                metadata.Add("Id", this.externalId);
                metadata.Add("Name", System.Security.SecurityElement.Escape(this.title));
                this.GetMetadata(metadata);

                StringBuilder metadataXml = new StringBuilder();
                metadataXml.Append("<Metadata xmlns=\"" + EclProvider.EcommerceEclNs.NamespaceName + "\">");
                foreach (var metadataName in metadata.Keys)
                {                  
                    var metadataValue = metadata[metadataName];
                    bool isXml = metadataName.EndsWith("__xml");
                    var name = metadataName.Replace("__xml", "");
                    if ( metadataValue is string )
                    {
                        metadataXml.Append("<" + name + ">");
                        if ( isXml )
                        {
                            metadataXml.Append(metadataValue);
                        }
                        else
                        {
                            metadataXml.Append(SecurityElement.Escape(metadataValue as string));
                        }                       
                        metadataXml.Append("</" + name + ">");
                    }
                    else if ( metadataValue is IList )
                    {
                        foreach ( var value in metadataValue as IList<string> )
                        {
                            metadataXml.Append("<" + name + ">");
                            if ( isXml )
                            {
                                metadataXml.Append(value);
                            }
                            else
                            {
                                metadataXml.Append(SecurityElement.Escape(value));
                            }                          
                            metadataXml.Append("</" + name + ">");
                        }
                    }
                                     
                }
                metadataXml.Append("</Metadata>");
                return metadataXml.ToString();
            }
            set { throw new NotSupportedException(); }
        }

        /// <summary>
        /// Get implementation specific metadata
        /// </summary>
        /// <param name="metadata"></param>
        protected abstract void GetMetadata(Dictionary<string, object> metadata);

        /// <summary>
        /// Metadata XML Schema property
        /// </summary>
        public ISchemaDefinition MetadataXmlSchema
        {
            get
            {
                ISchemaDefinition schema = EclProvider.HostServices.CreateSchemaDefinition("Metadata", EclProvider.EcommerceEclNs.NamespaceName);
                schema.Fields.Add(EclProvider.HostServices.CreateSingleLineTextFieldDefinition("Id", "Id", 0, 1));
                schema.Fields.Add(EclProvider.HostServices.CreateSingleLineTextFieldDefinition("Name", "Name", 0, 1));
                this.BuildMetadataXmlSchema(schema);
                return schema;
            }
        }

        /// <summary>
        /// Append to metadata XML schema with implementation specific metadata definitions
        /// </summary>
        /// <param name="schema"></param>
        protected abstract void BuildMetadataXmlSchema(ISchemaDefinition schema);

        public virtual string ModifiedBy
        {
            get { return CreatedBy; }
        }

        public abstract IEclUri ParentId { get; }
        public abstract string DisplayTypeId { get; }

        public virtual string Dispatch(string command, string payloadVersion, string payload, out string responseVersion)
        {
            throw new NotSupportedException();
        }

        public virtual IContentLibraryItem Save(bool readback)
        {
            // as saving isn't supported, the result of saving is always the item itself
            return readback ? this : null;
        }

        public abstract string GetDirectLinkToPublished(IList<ITemplateAttribute> attributes);
    }
}
