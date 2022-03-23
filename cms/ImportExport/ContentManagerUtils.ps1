<#
.SYNOPSIS
   Utility functions for interfacing with Content Manager Core Service and Import/Export Service
.DESCRIPTION
    This script is intended to be included (dot-sourced) into other PowerShell scripts.
    Note: this script expects the following variables to be defined/set by the invoking script: 
        $cmsUrl
        $cmsAuth
        $cmsUserName (optional)
        $cmsUserPassword (optional)

    Based on:
    http://www.indivirtual.nl/blog/sdl-tridions-importexport-api-end-content-porter/
    http://www.indivirtual.nl/blog/set-rights-permissions-using-sdl-tridion-core-service-api/
#>

function Get-TempFolder($folderName)
{
    $tempFolder = Join-Path $env:ALLUSERSPROFILE "$folderName\"
    if (!(Test-Path $tempFolder)) 
    {
        New-Item -ItemType Directory -Path $tempFolder | Out-Null
    }
    Write-Verbose "Temp folder is '$tempFolder'"
    return $tempFolder
}

function Initialize-CoreServiceClient($importExportFolder, $tempFolder) 
{
    Write-Verbose "Initializing Core Service client using folder '$importExportFolder'"

    if ($importExportFolder.StartsWith("\\") -or !((New-Object System.IO.DriveInfo (New-Object System.IO.FileInfo $importExportFolder).Directory.Root.FullName).DriveType -eq "Fixed")) {
        # Copy all DLLs to a local accessible location, since we cannot load DLLs from a network location
        if (!$tempFolder)
        {
            $tempFolder = Get-TempFolder("DXA")
        }

        if (!(Test-Path "$tempFolder\Tridion.ContentManager.CoreService.Client.dll")) 
        {
            #Copy-Item works weird if destination folder does not exist
            if (!(Test-Path $tempFolder)) 
            {
                New-Item -ItemType Directory -Path $tempFolder | Out-Null
            }
            Write-Verbose "Copying assemblies from '$importExportFolder' to '$tempFolder' ..."
            Copy-Item "$importExportFolder\*" $tempFolder -Recurse -Force
            Write-Verbose "Done"
        }
        $importExportFolder = $tempFolder
    }

    Add-Type -assemblyName mscorlib
    Add-Type -assemblyName System.ServiceModel
    Add-Type -Path "$importExportFolder\Tridion.ContentManager.CoreService.Client.dll"

    Write-Verbose "Done."

    return $importExportFolder
}

function Initialize-ImportExport($importExportFolder, $tempFolder) 
{
    $importExportFolder = Initialize-CoreServiceClient $importExportFolder $tempFolder 

    Write-Verbose "Initializing Import/Export using folder '$importExportFolder'"

    Add-Type -Path "$importExportFolder\Tridion.Common.dll"
    Add-Type -Path "$importExportFolder\Tridion.ContentManager.ImportExport.Common.dll"
    Add-Type -Path "$importExportFolder\Tridion.ContentManager.ImportExport.Client.dll"

    Write-Verbose "Done."
}

function Set-ClientCredentials($wcfClient) {
    switch ($cmsAuth) {
        "Windows" {
            if ($cmsUserName) {
                Write-Verbose "Using Windows authentication with CMS user name: $cmsUserName"
                $wcfClient.ClientCredentials.Windows.ClientCredential = New-Object System.Net.NetworkCredential $cmsUserName, $cmsUserPassword
            }
            else {
                Write-Verbose "Using Windows authentication with the current Windows user's credentials (no CMS user specified explicitly)." 
                $wcfClient.ClientCredentials.Windows.ClientCredential = [System.Net.CredentialCache]::DefaultNetworkCredentials;
            }
        }
        "Basic" {
            if (!$cmsUserName -or !$cmsUserPassword) {
                throw "CMS user name and password must be specified for Basic authentication."
            }
            Write-Verbose "Using Basic authentication with CMS user name: $cmsUserName"
            $wcfClient.ClientCredentials.UserName.UserName = $cmsUserName
            $wcfClient.ClientCredentials.UserName.Password = $cmsUserPassword
        }
    }

}

function Get-ImportExportServiceClient {
    param(
        [parameter(Mandatory=$false)]
        [ValidateSet("Service","Upload","Download")]
        [string]$type="Service"
    )
    Write-Verbose "Getting Import/Export Service Client with type '$type' for CMS URL '$cmsUrl' ..."

	$binding = New-Object System.ServiceModel.BasicHttpBinding
	$binding.MaxBufferPoolSize = [int]::MaxValue
	$binding.MaxReceivedMessageSize = [int]::MaxValue
	$binding.ReaderQuotas.MaxArrayLength = [int]::MaxValue
	$binding.ReaderQuotas.MaxBytesPerRead = [int]::MaxValue
	$binding.ReaderQuotas.MaxStringContentLength = [int]::MaxValue
	$binding.ReaderQuotas.MaxNameTableCharCount = [int]::MaxValue


	switch($type)
	{
		"Service" {
            if ($cmsUrl.StartsWith("https")) { $binding.Security.Mode = "Transport" }
            else { $binding.Security.Mode = "TransportCredentialOnly" }
			$binding.Security.Transport.ClientCredentialType = $cmsAuth
			$endpoint = New-Object System.ServiceModel.EndpointAddress ($cmsUrl + "webservices/ImportExportService201501.svc/basicHttp")
			$client = New-Object Tridion.ContentManager.ImportExport.Client.ImportExportServiceClient $binding,$endpoint
		}
 
		"Download" {
            if ($cmsUrl.StartsWith("https")) { $binding.Security.Mode = "Transport" }
            else { $binding.Security.Mode = "TransportCredentialOnly" }
			$binding.Security.Transport.ClientCredentialType = $cmsAuth
			$binding.TransferMode = "StreamedResponse"
			$binding.MessageEncoding = "Mtom"
			$endpoint = New-Object System.ServiceModel.EndpointAddress ($cmsUrl + "webservices/ImportExportService201501.svc/streamDownload_basicHttp")	
			$client = New-Object Tridion.ContentManager.ImportExport.Client.ImportExportStreamDownloadClient $binding,$endpoint
		}
 
		"Upload" {
            if ($cmsUrl.StartsWith("https")) { $binding.Security.Mode = "Transport" }
            else { $binding.Security.Mode = "None" }
			$binding.TransferMode = "StreamedRequest"
			$binding.MessageEncoding = "Mtom"
			$endpoint = New-Object System.ServiceModel.EndpointAddress ($cmsUrl + "webservices/ImportExportService201501.svc/streamUpload_basicHttp")
			$client = New-Object Tridion.ContentManager.ImportExport.Client.ImportExportStreamUploadClient $binding,$endpoint
		}
	}

    Set-ClientCredentials($client)

    Write-Verbose $client.Endpoint.Address
    return $client
}

function Get-CoreServiceClient {
    param(
        [parameter(Mandatory=$false)]
        [ValidateSet("Service","Upload","Download")]
        [string]$type="Service"
    )        
    Write-Verbose "Getting Core Service Client with type '$type' for CMS URL '$cmsUrl' ..."

	$binding = New-Object System.ServiceModel.BasicHttpBinding
	$binding.MaxBufferPoolSize = [int]::MaxValue
	$binding.MaxReceivedMessageSize = [int]::MaxValue
	$binding.ReaderQuotas.MaxArrayLength = [int]::MaxValue
	$binding.ReaderQuotas.MaxBytesPerRead = [int]::MaxValue
	$binding.ReaderQuotas.MaxStringContentLength = [int]::MaxValue
	$binding.ReaderQuotas.MaxNameTableCharCount = [int]::MaxValue

	switch($type)
	{
		"Service" {
            if ($cmsUrl.StartsWith("https")) { $binding.Security.Mode = "Transport" }
            else { $binding.Security.Mode = "TransportCredentialOnly" }
			$binding.Security.Transport.ClientCredentialType = $cmsAuth
			$endpoint = New-Object System.ServiceModel.EndpointAddress ($cmsUrl + "webservices/CoreService201501.svc/basicHttp")
			$client = New-Object Tridion.ContentManager.CoreService.Client.CoreServiceClient $binding,$endpoint
        }
	}

    Set-ClientCredentials($client)

    Write-Verbose $client.Endpoint.Address

    if ($type -eq "Service")
    {
        $cmsUser = $client.GetCurrentUser()
        $cmsUserTitle = $cmsUser.Title
        $cmsUserDescription = $cmsUser.Description
        Write-Verbose "Connected to Core Service as user '$cmsUserTitle' ($cmsUserDescription)" 
    }

    return $client;
}


$groups = $null 

function Get-Group($groupName, $coreServiceClient) {
    if (!$groups)
    {
        $groupsFilter = New-Object Tridion.ContentManager.CoreService.Client.groupsFilterData
        $groups = $coreServiceClient.GetSystemWideList($groupsFilter)
    }

    $group = $groups | Where { $_.Title -eq $groupName }
    if (!$group)
    {
        $group = New-Object Tridion.ContentManager.CoreService.Client.GroupData
        $group.Title = $groupName
        $group.Description = $groupName

        $readOptions = New-Object Tridion.ContentManager.CoreService.Client.ReadOptions
        $group = $coreServiceClient.Create($group, $readOptions)
        Write-Host "Created Group '$groupName': " $group.Id

        $groups += $group
    }

    return $group
}


function Set-AccessControlEntry($container, $trustee, $rights, $permissions) 
{
    # Remove any existing entries for this trustee
    $aces = $container.AccessControlList.AccessControlEntries | Where { $_.Trustee.IdRef -ne $trustee.Id }

    # If there is any ACE with UnknownByClient flag (e.g. BPT Management Rights since we're using a 7.1 client), the entire ACL won't get updated. As a work-around, we remove the ACE.
    $aces = $aces | Where { -not $_.AllowedRights.HasFlag([Tridion.ContentManager.CoreService.Client.Rights]::UnknownByClient) }

    $trusteeLink = New-Object Tridion.ContentManager.CoreService.Client.LinkToTrusteeData
    $trusteeLink.IdRef = $trustee.Id
 
    $ace = New-Object Tridion.ContentManager.CoreService.Client.AccessControlEntryData
    $ace.Trustee = $trusteeLink
    if ($rights) 
    {
        $ace.AllowedRights = $rights
    }
    if ($permissions) 
    {
        $ace.AllowedPermissions = $permissions
        $container.IsPermissionsInheritanceRoot = $true
    }
    $aces += $ace
    $container.AccessControlList.AccessControlEntries = $aces
}


function Import-Security($securityFilePath, $coreServiceClient) 
{
    Write-Host "Importing security settings from file '$securityFilePath'..."

    [xml]$securityXml = Get-Content $securityFilePath

    foreach ($publicationElement in $securityXml.export.publication) 
    {
        $publicationWebDavUrl = $publicationElement.path
        $publication = $coreServiceClient.TryRead($publicationWebDavUrl, $null)
        if ($publication)
        {
            Write-Host "Publication '$publicationWebDavUrl' ($($publication.Id)):"
            foreach ($entry in $publicationElement.rights) 
            {
                $group = Get-Group $entry.group $coreServiceClient
                $rights = $entry.InnerText
                Write-Host "`tGroup '$($group.Title)' ($($group.Id)) Rights: " $rights
                Set-AccessControlEntry $publication $group $rights $null
            }
            $coreServiceClient.Update($publication, $null)
        }
        else
        {
            Write-Warning "Publication '$publicationWebDavUrl' not found."
        }
    }

    foreach ($orgItemElement in $securityXml.export.organizationalItem) 
    {
        $orgItemWebDavUrl = $orgItemElement.path
        $orgItem = $coreServiceClient.TryRead($orgItemWebDavUrl, $null)
        if ($orgItem)
        {
            Write-Host "Organizational Item '$orgItemWebDavUrl' ($($orgItem.Id)):"
            foreach ($entry in $orgItemElement.permissions) 
            {
                $group = Get-Group $entry.group $coreServiceClient
                $permissions = $entry.InnerText
                Write-Host "`tGroup '$($group.Title)' ($($group.Id)) Permissions: " $permissions
                Set-AccessControlEntry $orgItem $group $null $permissions
             }
            $coreServiceClient.Update($orgItem, $null)
        }
        else
        {
            Write-Warning "Organizational Item '$orgItemWebDavUrl' not found."
        }
    }
}


function Invoke-UploadPackageFromFile($packageLocation) {
    Write-Verbose "Uploading package from file ..."
    Write-Verbose "PackageLocation is '$packageLocation'"
	$uploadService = Get-ImportExportServiceClient -type Upload
	try {
		$packageStream = [IO.File]::OpenRead($packageLocation)
		$uploadService.UploadPackageStream($packageStream)
	}
	finally {
		if ($packageStream -ne $null){
			$packageStream.Dispose()	
		}
		if ($uploadService -ne $null){
			$uploadService.Dispose()	
		}
	}
    Write-Verbose "Done"
}
 
function Wait-ImportExportFinish($serviceClient, $processId) {
    Write-Verbose "Waiting for Import/Export Process '$processId' to finish ..."

    $waited = 0
    $interval = 5
	do {
		$processState = $serviceClient.GetProcessState($processId)
		if ("Finished", "Aborted", "AbortedByUser" -contains $processState) {
			break;
		}

        Start-Sleep -Seconds $interval
        $waited = $waited + $interval
        Write-Verbose "($waited seconds)"
	} while ($true)

    Write-Verbose "Process State is '$processState'"
    return $processState
}

function Invoke-DownloadLog($processId, $path) {
    Write-Verbose "Downloading log for Import/Export Process '$processId' to '$path' ..."
    $downloadClient = Get-ImportExportServiceClient -type Download
    try {
	    $packageStream = $downloadClient.DownloadProcessLogFile($processId, $true)
	    $fileStream = [IO.File]::Create($path)
	    $packageStream.CopyTo($fileStream)
    }	
    finally { 
	    if ($fileStream -ne $null) {
		    $fileStream.Dispose()
	    }
	    if ($packageStream -ne $null) {
		    $packageStream.Dispose()
	    }
    }
    Write-Verbose "Done"
}


function Invoke-DownloadPackage($processId, $path) {    
    Write-Verbose "Downloading package for Import/Export Process '$processId' to '$path' ..."
    $downloadService = Get-ImportExportServiceClient -type Download
    try {
        $packageStream = $downloadService.DownloadPackage($processId, $true)    
        $fileStream = [IO.File]::Create($path)
        $packageStream.CopyTo( $fileStream)
    }    
    finally { 
        if ($fileStream -ne $null) {
            $fileStream.Dispose()
        }
        if ($packageStream -ne $null) {
            $packageStream.Dispose()
        }
    }
}


# Deprecated (DXA 1.2) function. Use Import-CmPackage instead. 
function Invoke-Upload($mapping, $packageFullPath, $tempFolder) 
{
    Write-Warning "Using deprecated Invoke-Upload function. Use Import-CmPackage instead."
    Import-CmPackage $packageFullPath $tempFolder $mapping
}


function Import-CmPackage($packageFullPath, $tempFolder, $mappings = $null)
{
    Write-Host "Uploading package '$packageFullPath' ..."
    $filename = (Get-ChildItem $packageFullPath).BaseName
    $extension = (Get-ChildItem $packageFullPath).Extension
    $uploadId = Invoke-UploadPackageFromFile $packageFullPath

    Write-Host "Importing content ..."
    $importInstruction = New-Object Tridion.ContentManager.ImportExport.ImportInstruction
    #$importInstruction.LogLevel = "Debug"
    $importInstruction.LogLevel = "Normal"
    $importInstruction.CreateUndoPackage = $false
    $importInstruction.SchemaSynchronizeFlags = "FixNamespace, RemoveUnknownFields, RemoveAdditionalValues, ApplyDefaultValuesForMissingMandatoryFields, ConvertFieldType"

    if ($mappings)
    {
        Write-Verbose "Using mappings:"
        $mappings | ForEach-Object { Write-Verbose "`t'$($_.ExportUrl)' -> '$($_.ImportUrl)'" }
        $importInstruction.UserMappings = [Tridion.ContentManager.ImportExport.Packaging.Mapping[]]($mappings)
    }
    
    $importExportClient = Get-ImportExportServiceClient
    $processId = $importExportClient.StartImport($uploadId, ($importInstruction))
    $state = Wait-ImportExportFinish $importExportClient $processId
    Write-Host $state
    $importExportClient.Dispose()

    # Download log and output it
    $importLogFullPath = "$($tempFolder)$($filename)-import.log"
    Invoke-DownloadLog $processId $importLogFullPath
    Get-Content $importLogFullPath

    if ($state -ne "Finished") 
    {
        throw "An error occured while importing '$packageFullPath'"
    }
}


function Export-CmPackage($targetFile, $selection, $exportInstruction)
{
    Write-Host "Exporting content..."
    $importExportClient = Get-ImportExportServiceClient
    $processId = $importExportClient.StartExport($selection, $exportInstruction)
    $state = Wait-ImportExportFinish $importExportClient $processId
    Write-Host $state

    # Download log and output it
    $exportLogFullPath = "$($tempFolder)$($exportType)-export.log" 
    Invoke-DownloadLog $processId $exportLogFullPath
    Get-Content $exportLogFullPath

    Write-Host "Downloading Export Package to '$targetFile' ..."
    Invoke-DownloadPackage $processId $targetFile
}

function Encode-WebDav($path) 
{
    if(!$path.StartsWith("/webdav/"))
    {
        $path = "/webdav/" + $path.TrimStart('/');
    }
    return [uri]::EscapeUriString($path).Replace(".","%2E");
}

function Get-TcmUri($itemId)
{
    if ($itemId.StartsWith("tcm:"))
    {
        return $itemId
    }
    return $coreServiceClient.GetTcmUri($itemId, $null, $null)
}

function Get-ImportMappings($masterPublication, $siteTypePublication, $contentPublication, $sitePublication, $rootFolder, $rootStructureGroup)
{
    return (
        (New-Object Tridion.ContentManager.ImportExport.Packaging.V2013.Mapping2013("Publication", "/webdav/100 Master", (Encode-WebDav $masterPublication))),
        (New-Object Tridion.ContentManager.ImportExport.Packaging.V2013.Mapping2013("Folder", "/webdav/100 Master/Building Blocks", (Encode-WebDav "$masterPublication/$rootFolder"))),
        (New-Object Tridion.ContentManager.ImportExport.Packaging.V2013.Mapping2013("StructureGroup", "/webdav/100 Master/Home", (Encode-WebDav "$masterPublication/$rootStructureGroup"))),
        (New-Object Tridion.ContentManager.ImportExport.Packaging.V2013.Mapping2013("Publication", "/webdav/110 DXA Site Type", (Encode-WebDav $siteTypePublication))),
        (New-Object Tridion.ContentManager.ImportExport.Packaging.V2013.Mapping2013("Folder", "/webdav/110 DXA Site Type/Building Blocks", (Encode-WebDav "$siteTypePublication/$rootFolder"))),
        (New-Object Tridion.ContentManager.ImportExport.Packaging.V2013.Mapping2013("StructureGroup", "/webdav/110 DXA Site Type/Home", (Encode-WebDav "$siteTypePublication/$rootStructureGroup"))),
        (New-Object Tridion.ContentManager.ImportExport.Packaging.V2013.Mapping2013("Publication", "/webdav/200 Example Content", (Encode-WebDav $contentPublication))),
        (New-Object Tridion.ContentManager.ImportExport.Packaging.V2013.Mapping2013("Folder", "/webdav/200 Example Content/Building Blocks", (Encode-WebDav "$contentPublication/$rootFolder"))),
        (New-Object Tridion.ContentManager.ImportExport.Packaging.V2013.Mapping2013("StructureGroup", "/webdav/200 Example Content/Home", (Encode-WebDav "$contentPublication/$rootStructureGroup"))),
        (New-Object Tridion.ContentManager.ImportExport.Packaging.V2013.Mapping2013("Publication", "/webdav/400 Example Site", (Encode-WebDav $sitePublication))),
        (New-Object Tridion.ContentManager.ImportExport.Packaging.V2013.Mapping2013("Folder", "/webdav/400 Example Site/Building Blocks", (Encode-WebDav "$sitePublication/$rootFolder"))),
        (New-Object Tridion.ContentManager.ImportExport.Packaging.V2013.Mapping2013("StructureGroup", "/webdav/400 Example Site/Home", (Encode-WebDav "$sitePublication/$rootStructureGroup")))
        )
}


function Add-ComponentPresentation($componentId, $templateId, $pageId, $insertIndex = -1) 
{
    Write-Host "Adding Component Presentation ('$componentId', '$templateId') to Page '$pageId' ..."

    $page = $coreServiceClient.Read($pageId, $defaultReadOptions)
    $component = $coreServiceClient.Read($componentId, $defaultReadOptions)
    $template = $coreServiceClient.Read($templateId, $defaultReadOptions)

    $cps = $page.ComponentPresentations
    $cpList = new-object 'System.Collections.Generic.List[object]'

    foreach ($cp in $cps)
    {
        #CP already exists on page
        if (($cp.ComponentTemplate.IdRef -eq $template.Id) -and ($cp.Component.IdRef -eq $component.Id))
        {
            Write-Host "The Component Presentation is already on the Page."
            return
        }
        $cpList.Add($cp)
    }

    $cp = New-Object Tridion.ContentManager.CoreService.Client.ComponentPresentationData
    $cp.Component = New-Object Tridion.ContentManager.CoreService.Client.LinkToComponentData
    $cp.Component.IdRef = $component.Id
    $cp.ComponentTemplate = New-Object Tridion.ContentManager.CoreService.Client.LinkToComponentTemplateData
    $cp.ComponentTemplate.IdRef = $template.Id
    if($insertIndex -ge 0 -and $insertIndex -lt $cpList.Count)
    {
        $cpList.Insert($insertIndex, $cp)
    }
    else
    {
        $cpList.Add($cp)
    }

    $page.ComponentPresentations = $cpList.ToArray()
    $coreServiceClient.Update($page, $null)
}

function Add-TemplateToCompound($addedTbbId, $compoundTemplateId) 
{
    Write-Host "Adding Template '$addedTbbId' to Compound Template '$compoundTemplateId' ..."

    $addedTbbId = Get-TcmUri($addedTbbId)

    $compoundTemplate = $coreServiceClient.Read($compoundTemplateId, $defaultReadOptions)
    $content = [xml] $compoundTemplate.Content
    $invokedTemplates = $content.CompoundTemplate.TemplateInvocation.Template.href
    if ($invokedTemplates -contains $addedTbbId)
    {      
		Write-Host "TBB is already present."
        return
    }

    $namespaceUri = $content.CompoundTemplate.xmlns
    $templateInvocation = $content.CreateElement("TemplateInvocation", $namespaceUri)
    $template = $content.CreateElement("Template", $namespaceUri)
    $templateInvocation.AppendChild($template) | Out-Null
    $templateHref = $content.CreateAttribute("xlink:href", "http://www.w3.org/1999/xlink")
    $templateHref.Value = $addedTbbId
    $template.SetAttributeNode($templateHref) | Out-Null
    $content.DocumentElement.AppendChild($templateInvocation) | Out-Null

    $compoundTemplate.Content = $content.OuterXml
    $coreServiceClient.Update($compoundTemplate, $null)
}

function Add-MetadataToItem($itemId, $schemaId, $metadata)
{
    Write-Host "Adding Metadata based on Schema '$schemaId' to Item '$itemId' ..."

    $schemaId = Get-TcmUri($schemaId)

    $item = $coreServiceClient.Read($itemId, $defaultReadOptions)
    if ($item.MetadataSchema -and ($item.MetadataSchema.IdRef -ne "tcm:0-0-0") -and ($item.MetadataSchema.IdRef -ne $schemaId))
    {
        Write-Warning "Item '$itemId' already has Metadata Schema '$($item.MetadataSchema.IdRef)'. Skipping this step."
        return
    }

    $item.MetadataSchema = New-Object Tridion.ContentManager.CoreService.Client.LinkToSchemaData
    $item.MetadataSchema.IdRef = $schemaId
    $item.Metadata = $metadata
    $coreServiceClient.Update($item, $null)
}
