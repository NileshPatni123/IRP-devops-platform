# Task 11: records the full container lifecycle into evidence\docker-lifecycle.log
#   .\scripts\docker-lifecycle.ps1
Set-Location (Split-Path $PSScriptRoot -Parent)
New-Item -ItemType Directory -Force evidence | Out-Null
Start-Transcript -Path .\evidence\docker-lifecycle.log -Force
docker version --format "Docker {{.Server.Version}}"
docker build -t irp-portal:1.0.0 -t irp-portal:latest .
docker image ls irp-portal
docker image inspect irp-portal:1.0.0 --format "Size={{.Size}} Created={{.Created}}"
docker run -d --name irp-portal -p 8080:8080 -e APP_ENVIRONMENT=docker-demo irp-portal:1.0.0
Start-Sleep -Seconds 40
docker ps
docker logs --tail 20 irp-portal
curl.exe -s http://localhost:8080/actuator/health
docker stop irp-portal
docker ps -a --filter name=irp-portal
docker start irp-portal
docker restart irp-portal
docker stats --no-stream irp-portal
docker rm -f irp-portal
docker ps -a --filter name=irp-portal
Stop-Transcript
