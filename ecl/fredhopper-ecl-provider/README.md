  Fredhopper ECL Provider
===========================

## Installation

* Create a directory called 'Fredhopper-ECL-Provider' under C:\ProgramData\SDL\SDL Tridion\External Content Library\AddInPipeline\Addins\
* Either use Visual Studio to compile the DLLs to this directory or copy the code manually to this location
* Copy the Themes directory to the created directory
* Open up your ExternalContentLibrary.xml in your Tridion config directory and add the following:

```
  <MountPoint type="Fredhopper-ECL-Provider" id="fredhopper" rootItemName="Fredhopper E-Commerce">
      <StubFolders>
        <StubFolder id="[STUB FOLDER TCM-URI]" />
      </StubFolders>
      <PrivilegedUserName>[TRIDION ADMIN USER]]</PrivilegedUserName>
      <EndpointAddress xmlns="http://sdl.com/ecl/ecommerce">
      [Fredhopper endpoint address, e.g. http://localhost:8180/fredhopper-ws/services/FASWebService]
      </EndpointAddress>
      <MaxReceivedMessageSize>[optional paramter to define max receive message size on the endpoint, e.g. 1000000]</MaxReceivedMessageSize>
      <ProductPageSize xmlns="http://sdl.com/ecl/ecommerce">
        [Max items per page to be presented when navigating products & doing searches, e.g. 100]
      </ProductPageSize>
      <CategoryPageSize xmlns="http://sdl.com/ecl/ecommerce">
        [Max items per page to be presented when navigating selectable categories, e.g. 100]
      </CategoryPageSize>
	  <CategoryMaxDepth xmlns="http://sdl.com/ecl/ecommerce">
        [Max depth to go to build up the category tree e.g. 4]
      </CategoryMaxDepth>
	    <PublicationConfigurations xmlns="http://sdl.com/ecl/ecommerce">
		    <PublicationConfiguration xmlns="http://sdl.com/ecl/ecommerce"
            publicationIds = "[publication IDs comma separated]"
            fallback="[true/false - to be used if there is no configuration found for a particular publication]">
			          <Locale xmlns="http://sdl.com/ecl/ecommerce">[Locale e.g. en-GB]</Locale>
                <Universe xmlns="http://sdl.com/ecl/ecommerce">[Universe e.g. catalog01]</Universe>
            <ModelMappings xmlns="http://sdl.com/ecl/ecommerce">
              [Model mappings to Fredhopper model defined in Business Manager, e.g  name=name;description=description;price=price;thumbnailUrl=_thumburl;primaryImageUrl=_imageurl]
            </ModelMappings>
            <Filters>
            	<Filter name="[facet name]" value="[facet value"/>
            	...
            </Filters>
        </PublicationConfiguration>
        ...
	  </PublicationConfigurations>
  </MountPoint>
```

* Make sure that the id of the mountpoint is named 'fredhopper', otherwise the ECL mappings on the DXA side will not work.
* Verify that the multimedia type has been created for ECL with MIME type = 'application/externalcontentlibrary' and extension = 'ecl'
* Restart SDL Tridion services and verify that you can browse the Fredhopper product catalog through SDL Web
