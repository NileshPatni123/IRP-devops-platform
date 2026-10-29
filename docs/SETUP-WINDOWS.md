
# Setup Guide: Run the IRP DevOps Platform on Windows

Follow the steps in order. Steps 1–3 get the application running. The remaining steps are optional depending on what you want to configure.

| Goal | Steps |
|---|---|
| Run the application with Docker | 1, 2, 3 |
| Develop using VS Code | 1, 2, 4 |
| Use PostgreSQL | 1, 2, 5 |
| Configure Jenkins CI/CD | 1, 2, 6 |
| Run Selenium tests | 1, 2, 7 |
| Configure Ansible | 1, 2, 8 |
| Enable monitoring | 1, 2, 9 |

## Step 1. Install the tools

Open PowerShell and install the tools using `winget` on Windows 10/11:

```powershell
winget install -e --id Microsoft.VisualStudioCode
winget install -e --id Git.Git
winget install -e --id EclipseAdoptium.Temurin.17.JDK
winget install -e --id Apache.Maven
winget install -e --id Docker.DockerDesktop
winget install -e --id Google.Chrome
winget install -e --id Python.Python.3.12
```

Close and reopen PowerShell after installation. Restart Windows if required, then launch Docker Desktop and wait until its engine is running.

Verify the tools:

```powershell
java -version
mvn -version
git --version
docker --version
docker run --rm hello-world
python --version
```

If `mvn` is not recognized, install Maven manually from the official Apache Maven website and add its `bin` directory to your Windows PATH.

## Step 2. Open the project in VS Code

1. Extract the project into a short path, for example `C:\projects\irp-devops-platform`.
2. Open VS Code and select **File > Open Folder**.
3. Open the project root folder containing `pom.xml` and `Dockerfile`.
4. Install the recommended Java, Spring Boot, Docker, YAML, Ansible, and PostgreSQL extensions if needed.
5. Select **Terminal > New Terminal**.

If a local PowerShell script is blocked, you can allow scripts for the current terminal session only:

```powershell
Set-ExecutionPolicy -Scope Process -ExecutionPolicy Bypass -Force
```

Check the project task deliverables:

```powershell
python scripts/verify-tasks.py
```

## Step 3. Run the application with Docker

Make sure Docker Desktop is running. From the project root, execute:

```powershell
docker compose up -d --build
```

Open the application:

- Portal: http://localhost:8080
- Health endpoint: http://localhost:8080/actuator/health

Check the containers:

```powershell
docker compose ps
docker compose logs --tail=100 portal
```

The portal should become healthy after startup. The health endpoint should report a status of `UP`.

Stop the application:

```powershell
docker compose down
```

To delete the local H2 data volume as well:

```powershell
docker compose down -v
```

**Warning:** The `-v` option deletes the data stored in the Compose-managed volume.

## Step 4. Run and edit from VS Code without Docker

Build and test the application:

```powershell
mvn -B clean verify
```

Look for `BUILD SUCCESS`.

Start the application:

```powershell
mvn spring-boot:run
```

Alternatively, use the VS Code Run/Debug configuration for the H2 profile if available.

Open http://localhost:8080.

To change the user interface, edit:

`src/main/resources/static/index.html`

Save the file and refresh your browser. Depending on the changes, you may need to restart the application.

The default H2 configuration uses local development storage. Its persistence behavior depends on the configured H2 URL.

## Step 5. Use PostgreSQL

The PostgreSQL Compose configuration requires a password supplied through the `POSTGRES_PASSWORD` environment variable. Do not commit passwords to source files or documentation.

### Option A — Run PostgreSQL and the portal in Docker

In PowerShell, set a strong, unique password for this local setup:

```powershell
$securePassword = Read-Host "Enter a PostgreSQL password" -AsSecureString
$env:POSTGRES_PASSWORD = [System.Net.NetworkCredential]::new("", $securePassword).Password
```

Validate the Compose configuration:

```powershell
docker compose -f docker-compose.postgres.yml config -q
```

Start the services:

```powershell
docker compose -f docker-compose.postgres.yml up -d --build
```

Open http://localhost:8080.

Check the services:

```powershell
docker compose -f docker-compose.postgres.yml ps
docker compose -f docker-compose.postgres.yml logs --tail=100
```

The PostgreSQL data is stored in the `irp-pgdata` volume and normally survives `docker compose down`.

### Option B — Run PostgreSQL in Docker and the app from VS Code

Set `POSTGRES_PASSWORD` in PowerShell as shown above, then start the database:

```powershell
docker compose -f docker-compose.postgres.yml up -d postgres
```

Run the application using the PostgreSQL profile:

```powershell
mvn spring-boot:run "-Dspring-boot.run.profiles=postgres"
```

Open http://localhost:8080.

For the PostgreSQL extension in VS Code, use:

- Host: `localhost`
- Port: `5432`
- Database: `irp`
- Username: `irp`
- Password: the password you configured locally

You can inspect repository records from PowerShell:

```powershell
docker exec -it irp-postgres psql -U irp -d irp -c "SELECT id, title, status FROM repository_records;"
```

Flyway applies the database migrations during application startup.

**Important:** The Compose environment variable must match the actual password configured in the existing PostgreSQL database. Changing an environment variable alone does not rotate an existing database user's password. If a database volume already exists, do not delete it just to change credentials.

## Step 6. Configure Jenkins CI/CD

Jenkins is optional. This project can be built and tested without Jenkins.

1. Download the Jenkins LTS WAR file from the official Jenkins website.
2. Start Jenkins using Java:

   ```powershell
   java -jar C:\jenkins\jenkins.war --httpPort=9090
   ```

3. Open http://localhost:9090.
4. Follow the setup wizard, enter the initial administrator password displayed in the console, install the suggested plugins, and create an administrator account.
5. Install or verify the Pipeline, Git, GitHub, JUnit, and Credentials Binding plugins if your pipeline needs them.

### Connect the project to GitHub

If the repository already exists on GitHub, use the existing remote instead of creating another one.

From the project root:

```powershell
git remote -v
git status
```

If the remote is not configured, add your repository URL:

```powershell
git remote add origin https://github.com/NileshPatni123/IRP-devops-platform.git
```

Only do this if `origin` does not already exist.

Commit and push your project after reviewing the staged files:

```powershell
git add -A
git --no-pager diff --cached --check
git commit -m "Initial commit: IRP DevOps platform"
git push -u origin main
```

If the initial commit already exists, do not repeat the initial-commit command; simply push the appropriate commit.

### Create a Jenkins pipeline

1. Select **New Item > Pipeline**.
2. Give the job a descriptive name, such as `irp-portal`.
3. Under Pipeline, choose **Pipeline script from SCM**.
4. Select Git and enter the repository URL.
5. Set the branch to `*/main` and the script path to `Jenkinsfile`.
6. Save and run the job.

The exact stages, parameters, and deployment behavior depend on the contents of `Jenkinsfile`. Review its configuration before assuming a successful build will deploy a container.

For Docker Hub publishing, create a Docker Hub access token and store it in Jenkins Credentials. Never place registry credentials directly in the Jenkinsfile or repository.

## Step 7. Run Selenium tests

Start the application first. The default Selenium base URL is `http://localhost:8080`.

Open a separate PowerShell terminal:

```powershell
cd selenium-tests
mvn -B test
```

To run with a visible browser, if supported by the test configuration:

```powershell
mvn -B test "-Dheadless=false"
```

Review the test summary in the Maven output and the reports under `selenium-tests\target\surefire-reports`.

A failed test may save a screenshot under `selenium-tests\target\screenshots`, depending on the test configuration.

## Step 8. Configure Ansible

Ansible is generally run through WSL2 on Windows.

Install Ubuntu if WSL2 is not already configured:

```powershell
wsl --install -d Ubuntu
```

Restart Windows if prompted, open Ubuntu, and follow the instructions in `docs/RUNBOOK.md` for Tasks 13 and 14.

Run the playbooks according to the runbook and inspect the output. When a playbook is designed to be idempotent, a second successful run should report no unnecessary changes.

## Step 9. Optional monitoring

Start the monitoring profile:

```powershell
docker compose --profile monitoring up -d --build
```

Open:

- Prometheus: http://localhost:9091
- Grafana: http://localhost:3000

Check the Grafana provisioning and authentication configuration before logging in. Do not assume default credentials are secure or enabled.

Stop the monitoring services:

```powershell
docker compose --profile monitoring down
```

## Ports used

| Port | Service |
|---|---|
| 8080 | Portal |
| 8081 | Optional alternate portal/test port |
| 9090 | Jenkins |
| 5432 | PostgreSQL |
| 4444 | Selenium, if configured |
| 9091 | Prometheus |
| 3000 | Grafana |

Ports can vary according to environment variables and Compose configuration.

## Troubleshooting

| Problem | Suggested check |
|---|---|
| Docker cannot connect | Start Docker Desktop and wait for the engine to be ready. |
| Port 8080 is already in use | Inspect the process listening on the port or configure a different `HTTP_PORT`. |
| Maven is not recognized | Reopen PowerShell and verify Maven is on PATH. |
| Wrong Java version | Run `java -version` and confirm a compatible JDK is installed. |
| PostgreSQL connection fails | Check the database container health, port mapping, username, and password. |
| Compose reports a missing password | Set `POSTGRES_PASSWORD` in the current PowerShell session before running Compose. |
| Build cannot download dependencies | Check network connectivity and any configured proxy, then retry. |
| Application is unhealthy | Inspect the container logs and the health endpoint before restarting. |

To stop the PostgreSQL services:

```powershell
docker compose -f docker-compose.postgres.yml down
```

To remove the PostgreSQL data volume as well, use `down -v` only when you intentionally want to delete the stored database.
