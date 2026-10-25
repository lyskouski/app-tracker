# Packs the Compose distributable (zwyka.exe with a bundled JRE) into an MSIX package.
# The package is unsigned: the Microsoft Store signs it on submission.
#
# Usage: pwsh build.ps1 -Version <x.y.z> [-Source <distributable folder>] [-Output <file.msix>]
param(
    [Parameter(Mandatory = $true)][string]$Version,
    [string]$Source = "../desktopApp/build/compose/binaries/main/app/zwyka",
    [string]$Output = "zwyka_Windows.msix"
)
$ErrorActionPreference = "Stop"

# MSIX requires a 4-part version, and the Store reserves the last part (has to be 0).
$parts = @($Version.Split('-')[0].Split('.'))
while ($parts.Count -lt 3) { $parts += "0" }
$msixVersion = ($parts[0..2] + "0") -join '.'

# The latest Windows SDK installed (makeappx, makepri).
$sdk = Get-ChildItem "${env:ProgramFiles(x86)}\Windows Kits\10\bin\10.*\x64\makeappx.exe" |
    Sort-Object { [version]$_.Directory.Parent.Name } | Select-Object -Last 1 | ForEach-Object { $_.DirectoryName }
if (-not $sdk) { throw "Windows SDK (makeappx.exe) is not found" }

$layout = Join-Path $PSScriptRoot "layout"
if (Test-Path $layout) { Remove-Item $layout -Recurse -Force }
Copy-Item $Source $layout -Recurse
Copy-Item (Join-Path $PSScriptRoot "Assets") $layout -Recurse
(Get-Content (Join-Path $PSScriptRoot "AppxManifest.xml") -Raw).Replace("{VERSION}", $msixVersion) |
    Set-Content (Join-Path $layout "AppxManifest.xml") -Encoding utf8

# Resources index: resolves the qualified icons (targetsize, altform-unplated) for the taskbar and Start menu.
$priConfig = Join-Path $PSScriptRoot "priconfig.xml"
& "$sdk\makepri.exe" createconfig /cf $priConfig /dq en-US /pv 10.0.0 /o
if ($LASTEXITCODE -ne 0) { throw "makepri createconfig failed" }
& "$sdk\makepri.exe" new /pr $layout /cf $priConfig /mn (Join-Path $layout "AppxManifest.xml") /of (Join-Path $layout "resources.pri") /o
if ($LASTEXITCODE -ne 0) { throw "makepri new failed" }

& "$sdk\makeappx.exe" pack /d $layout /p $Output /o
if ($LASTEXITCODE -ne 0) { throw "makeappx pack failed" }
Write-Host "Created $Output ($msixVersion)"
