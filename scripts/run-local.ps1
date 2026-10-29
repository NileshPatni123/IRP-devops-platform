# Runs the portal locally (Task 3). From the project root:
#   Set-ExecutionPolicy -Scope Process -ExecutionPolicy Bypass -Force
#   .\scripts\run-local.ps1
$ErrorActionPreference = "Stop"
Set-Location (Split-Path $PSScriptRoot -Parent)
if (-not (Get-Command mvn -ErrorAction SilentlyContinue)) { throw "Maven (mvn) is not installed or not on PATH." }
if (-not (Get-Command java -ErrorAction SilentlyContinue)) { throw "Java 17+ is not installed or not on PATH." }
mvn -B clean verify
if ($LASTEXITCODE -ne 0) { throw "Build failed. Fix the errors above before starting the app." }
Write-Host "Open http://localhost:8080" -ForegroundColor Green
mvn spring-boot:run
