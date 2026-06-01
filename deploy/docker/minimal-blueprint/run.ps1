# Run Mill minimal-blueprint locally via Docker.
[CmdletBinding()]
param(
    [string] $DataDir,
    [string] $Image,
    [int] $Port = 0,
    [switch] $Detach
)

$ErrorActionPreference = "Stop"
$ScriptDir = $PSScriptRoot

function Load-DotEnv {
    param([string] $Path)
    if (-not (Test-Path -LiteralPath $Path)) { return }
    Get-Content -LiteralPath $Path | ForEach-Object {
        $line = $_.Trim()
        if ($line -eq "" -or $line.StartsWith("#")) { return }
        if ($line -match '^\s*([A-Za-z_][A-Za-z0-9_]*)\s*=\s*(.*)$') {
            $name = $Matches[1]
            $value = $Matches[2].Trim().Trim('"').Trim("'")
            Set-Item -Path "env:$name" -Value $value
        }
    }
}

function Convert-ToDockerVolumePath {
    param([string] $Path)
    $resolved = (Resolve-Path -LiteralPath $Path).ProviderPath
    if ($IsWindows -or ($env:OS -match "Windows")) {
        $drive = $resolved.Substring(0, 1).ToLowerInvariant()
        $rest = $resolved.Substring(2).Replace("\", "/")
        return "/${drive}${rest}"
    }
    return $resolved
}

Load-DotEnv -Path (Join-Path $ScriptDir ".env")

if ($PSBoundParameters.ContainsKey("Image")) {
    # -Image on command line
} elseif ($env:MILL_IMAGE) {
    $Image = $env:MILL_IMAGE
} else {
    $Image = "qpointz/mill-service-minimal:latest"
}

if (-not $PSBoundParameters.ContainsKey("DataDir")) {
    $DataDir = if ($env:MILL_DATA_DIR) { $env:MILL_DATA_DIR } else { Join-Path $ScriptDir "data" }
}
if ($PSBoundParameters.ContainsKey("Port") -and $Port -gt 0) {
    # -Port on command line
} elseif ($env:MILL_HOST_PORT) {
    $Port = [int] $env:MILL_HOST_PORT
} else {
    $Port = 8080
}
$ContainerName = if ($env:MILL_CONTAINER_NAME) { $env:MILL_CONTAINER_NAME } else { "mill-minimal-blueprint" }

if (-not (Get-Command docker -ErrorAction SilentlyContinue)) {
    throw "docker not found in PATH"
}
if (-not [System.IO.Path]::IsPathRooted($DataDir)) {
    $DataDir = Join-Path $ScriptDir $DataDir
}
if (-not (Test-Path -LiteralPath $DataDir)) {
    New-Item -ItemType Directory -Force -Path $DataDir | Out-Null
}

$dataFiles = Get-ChildItem -LiteralPath $DataDir -Recurse -File -ErrorAction SilentlyContinue |
    Where-Object { $_.Extension -match '^\.(parquet|csv|avro|xlsx)$' }
if (-not $dataFiles) {
    Write-Warning "No data files under $DataDir. Add e.g. data\cities\cities.parquet -> /data/cities/cities.parquet in the container."
}

$DataDirAbs = Convert-ToDockerVolumePath $DataDir
$ConfigDirAbs = Convert-ToDockerVolumePath (Join-Path $ScriptDir "config")

# Ignore "No such container" when nothing is running yet (Stop treats docker stderr as error).
$prevErrorAction = $ErrorActionPreference
$ErrorActionPreference = "SilentlyContinue"
null = docker rm -f $ContainerName 2>&1
$ErrorActionPreference = $prevErrorAction

$dockerArgs = @(
    "run", "--rm", "--name", $ContainerName,
    "-p", "${Port}:8080",
    "-v", "${DataDirAbs}:/data:ro",
    "-v", "${ConfigDirAbs}/application.yml:/app/config/application.yml:ro",
    "-v", "${ConfigDirAbs}/flow.yml:/app/config/flow/flow.yml:ro",
    "-v", "${ConfigDirAbs}/auth.yml:/app/config/auth/auth.yml:ro"
)
if ($Detach) { $dockerArgs += "-d" } else { $dockerArgs += "-it" }
$dockerArgs += $Image

Write-Host "Image:      $Image"
Write-Host "Data host:  $DataDir"
Write-Host "Data mount: ${DataDirAbs}:/data:ro"
Write-Host "Config:     $ConfigDirAbs"
Write-Host "URL:    http://localhost:${Port}"
Write-Host ""

& docker @dockerArgs

if ($Detach) {
    Write-Host "Logs: docker logs -f $ContainerName"
}
