$ErrorActionPreference = 'Stop'

Write-Host 'OnScreenKeyboard - Minecraft 1.21.11 / Fabric Loader 0.19.5' -ForegroundColor Cyan

$java = Get-Command java -ErrorAction SilentlyContinue
if (-not $java) {
    throw 'Java was not found. Install Java 21 and run this script again.'
}

$javaVersion = (& java -version 2>&1 | Select-Object -First 1)
Write-Host "Using $javaVersion"

if (Test-Path '.\gradlew.bat') {
    & .\gradlew.bat build --no-daemon
} elseif (Get-Command gradle -ErrorAction SilentlyContinue) {
    & gradle build --no-daemon
} else {
    Write-Host 'Gradle is not installed and this source package does not include a Gradle wrapper.' -ForegroundColor Yellow
    Write-Host 'Use the included GitHub Actions workflow, or install Gradle 9.6 and run this script again.'
    exit 1
}

Write-Host ''
Write-Host 'Build complete. Look in build\libs\ for the jar.' -ForegroundColor Green
