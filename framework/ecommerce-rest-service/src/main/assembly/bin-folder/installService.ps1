# Install SDL Web E-Commerce REST micro service
#
# Java options and system properties to pass to the JVM when starting the service
# Separate double-quoted options with a comma. For example:
# $jvmoptions = "-Xrs", "-Xms256m", "-Xmx512m", "-Dmy.system.property='/a folder with a space in it/'"
$jvmoptions = "-Xrs", "-Xms256m", "-Xmx512m", "-Dorg.apache.tomcat.util.buf.UDecoder.ALLOW_ENCODED_SLASH=true", "-Dorg.apache.catalina.connector.CoyoteAdapter.ALLOW_BACKSLASH=true"


$currentFolder = Get-Location
$name="SDLWebECommerceService"
$displayName="SDL Web E-Commerce Service"
$description="SDL Web E-Commerce Service"
$serverPort="--server.port=8097"
$dependsOn=""
#set max size of request header to 64Kb
$maxHttpHeaderSize="--server.tomcat.max-http-header-size=65536"
$path=$PSScriptRoot
cd $path\..
$rootFolder = Get-Location
$procrun="procrun.exe"
$application=$path + "\" + $procrun
$fullPath=$path + "\" + $procrun

$filteredArgs = @()
foreach($arg in $args) {
    if ($arg -imatch "--DisplayName=""?(.+)""?") {
        $displayName=$Matches[1]
    } elseif ($arg -imatch "--Name=""?(.+)""?") {
        $name=$Matches[1]
    } elseif ($arg -imatch "--Description=""?(.+)""?") {
        $description=$Matches[1]
    } elseif ($arg -imatch "--server.port=""?[0-9]+") {
        $serverPort=$Matches[0]
    } elseif ($arg -imatch "--DependsOn=""?(.+)""?") {
        $dependsOn += $arg
    } elseif ($arg -imatch "-D.+") {
        $jvmoptions+=$arg
    } else {
        $filteredArgs += $arg
    }
}

$arguments = @()
$arguments += "//IS//" + $name
$arguments += "--DisplayName=" + $displayName
$arguments += "--Description=" + $description
$arguments += "--Install=" + $fullPath
$arguments += "--Jvm=auto"
$arguments += "--Startup=auto"
$arguments += "--LogLevel=Info"
$arguments += "--StartMode=jvm"
$arguments += "--StartPath=" + $rootFolder
$arguments += "--StartClass=com.sdl.ecommerce.service.WinServiceContainer"
$arguments += "--StartParams=start"
$arguments += "++StartParams=" + $serverPort
$arguments += "++StartParams=" + $maxHttpHeaderSize
$arguments += $dependsOn
foreach($arg in $filteredArgs) {
    $arguments += "++StartParams=" + $arg
}
$arguments += $jvmoptions | Foreach-Object{ '++JvmOptions=' + $_ }
$arguments += "--StopMode=jvm"
$arguments += "--StopClass=com.sdl.ecommerce.service.WinServiceContainer"
$arguments += "--StopParams=stop"

$classpath = ".\bin\*;.\lib\*;.\addons\*;.\config"
foreach ($folder in Get-ChildItem -path $rootFolder -recurse | ?{ $_.PSIsContainer } | Resolve-Path -relative | Where { $_ -match 'services*' })
{
    $classpath = $folder + ";" + $folder + "\*;" + $classpath
}
$arguments += "--Classpath=" + $classpath

# Check script is launched with Administrator permissions
$isAdministrator = ([Security.Principal.WindowsPrincipal] [Security.Principal.WindowsIdentity]::GetCurrent()).IsInRole([Security.Principal.WindowsBuiltInRole] "Administrator")
if ($isAdministrator -eq $False) {
    $Host.UI.WriteErrorLine("ERROR: Please ensure script is launched with Administrator rights")
    Exit
}

# Saving service name into file
$name | Out-File ".\config\serviceName.txt"

function checkServiceListening {
	if (! (Get-Command Test-NetConnection -ErrorAction SilentlyContinue)) {
        Write-Debug "This operating system cannot check for running services"
		Return
    }
    $port = $serverPort.split("=")[1]
    # Time limit in milliseconds
    $timeLimit = 60000
    $stopwatch = New-Object System.Diagnostics.Stopwatch
    $stopwatch.Start();
    # check port is listening
    $status = Test-NetConnection -Port $port -ComputerName localhost
    while(-not ($status.TcpTestSucceeded)) {
        Start-Sleep -Seconds 1
        $status = Test-NetConnection -Port $port -ComputerName localhost

        if ($stopwatch.ElapsedMilliseconds -gt $timeLimit) {
            $Host.UI.WriteErrorLine("ERROR: Unable to start service during timeframe of 60s")
            $Host.UI.WriteErrorLine("Please check logs and configuration files.")
            $stopwatch.Stop()
            Return
        }
    }
    $stopwatch.Stop()
    Write-host "Service '$name' successfully started and listening on port $port" -ForegroundColor Green
}

Try {
    Write-Host "Installing '$name' as windows service..." -ForegroundColor Green
    if (Get-Service $name -ErrorAction SilentlyContinue) {
        Write-Warning "Service '$name' already exists in system."
    } else {
        & $application $arguments
        Start-Sleep -s 3

        if (Get-Service $name -ErrorAction SilentlyContinue) {
            Write-Host "Service '$name' successfully installed." -ForegroundColor Green
        } else {
            $Host.UI.WriteErrorLine("ERROR: Unable to create the service '" + $name + "'")
            Exit
        }
    }

    if ((Get-Service $name -ErrorAction SilentlyContinue).Status -ne "Running") {
        Write-Host "Starting service '$name'..." -ForegroundColor Green
        & sc.exe start $name
        checkServiceListening
    } else {
        Write-Host "Service '$name' already started." -ForegroundColor Green
    }
} Finally {
    cd $currentFolder
}
