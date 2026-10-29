# Runbook: step by step, task by task

Run PowerShell commands from the project root unless stated. Every command that prints something worth keeping should be screenshotted into `evidence/`.

## Prerequisites (one time)
JDK 17+, Maven 3.9+, Git, Docker Desktop, Google Chrome (for local Selenium), Jenkins (Task 7). Check:
```powershell
java -version ; mvn -version ; git --version ; docker --version
```

## Fastest start: Docker only (no Maven needed)
```powershell
docker compose up -d --build        # first build takes a few minutes
```
Open http://localhost:8080. Stop with `docker compose down`. Optional monitoring:
```powershell
docker compose --profile monitoring up -d --build
```
Prometheus is at http://localhost:9091 (Status, Targets should show `irp-portal` UP) and Grafana at http://localhost:3000 (login admin / admin; the Prometheus datasource is pre-configured; import dashboard ID 4701 for JVM metrics).
Optional image scan in Jenkins: tick `SECURITY_SCAN` (Trivy, HIGH and CRITICAL; marks the stage unstable instead of blocking).

## Task 3: run locally
```powershell
Set-ExecutionPolicy -Scope Process -ExecutionPolicy Bypass -Force
.\scripts\run-local.ps1
```
Expect `BUILD SUCCESS`, then open http://localhost:8080. Screenshot the app and the build.

## Task 4: GitHub repository
1. On GitHub create an empty repo `institutional-repository-portal` (no README).
2. In the project folder:
```powershell
git init -b main
git add .
git commit -m "chore: initial application skeleton and project docs"
git remote add origin https://github.com/<you>/institutional-repository-portal.git
git push -u origin main
git checkout -b develop ; git push -u origin develop
```
3. In GitHub: Settings, Branches, protect `main` (require a pull request). Create 3 issues from the templates.

**Branch policy:** `main` is release-only; `develop` is integration; work on `feature/<issue>-<short-name>`, `bugfix/<issue>-<name>`, `release/<version>`. Commit messages: `type(scope): message` such as `feat(api): add record search`. Merge by pull request only.

## Task 5: feature branch, pull request, review
```powershell
git checkout develop
git checkout -b feature/12-record-search
# make a small real change, for example edit docs or UI text
git add . ; git commit -m "feat(search): trim search text and ignore case"
git push -u origin feature/12-record-search
git pull origin develop ; git log --oneline --graph -10
```
Open a pull request into `develop`, add at least two review comments (ask a classmate or comment on your own diff), resolve them, then merge. Screenshot the PR and the merge.

## Task 6: conflict, tag, baseline
```powershell
.\scripts\git-conflict-demo.ps1        # creates the conflict
git status                              # shows both modified
# edit src/main/resources/static/index.html, keep one line, delete <<<<<<< ======= >>>>>>> markers
git add src/main/resources/static/index.html
git commit -m "merge: resolve tagline conflict"
git checkout main ; git merge develop
git tag -a v1.0.0 -m "MVP release baseline" ; git push origin main --tags
```

## Task 9: Selenium locally
Start the app (Task 3) in one window, then in another:
```powershell
cd selenium-tests
mvn -B test                            # headless Chrome
mvn -B test -Dheadless=false           # watch the browser
```
Report: `selenium-tests\target\surefire-reports`. To prove the screenshot mechanism, stop the app or run with the defect (Task 10) and open `target\screenshots`.

## Task 7: Jenkins install and CI job
1. Install Jenkins LTS for Windows (https://www.jenkins.io/download/), open http://localhost:8080 (move Jenkins to port 9090 during install if the portal uses 8080; this runbook assumes **Jenkins on 9090**).
2. Install suggested plugins plus **Pipeline**, **Git**, **JUnit**. Manage Jenkins, Tools: add JDK 17 and Maven, or make sure `java` and `mvn` are on the system PATH of the Jenkins service.
3. New Item, Freestyle job `irp-ci`. Source: your GitHub repo, branch `*/develop`. Build trigger: **Poll SCM** `H/2 * * * *`. Build step: `mvn -B clean verify`. Post-build: archive `target/*.war`, publish JUnit `target/surefire-reports/*.xml`.
4. Build Now, then push a small commit and wait two minutes to show the trigger. Screenshot config, console log, artefact and the auto-triggered build.

## Task 8: Jenkinsfile pipeline and Tomcat deploy
1. New Item, **Pipeline**, “Pipeline script from SCM”, Git, script path `Jenkinsfile`.
2. Run **Build with Parameters**. The `ENVIRONMENT` and `HTTP_PORT` parameters are the parameterised settings. Jenkins needs Docker access: Docker Desktop must be running and the Jenkins service account must be able to run `docker` (on Windows run the Jenkins service as your user, or on Linux add `jenkins` to the `docker` group and restart).
3. The pipeline deploys the WAR to Tomcat inside the Docker image. Open http://localhost:8080 (the `HTTP_PORT`) and check the badge in the header: it shows the environment and version you passed.
4. Run again with `ENVIRONMENT=production` and `HTTP_PORT=8082` to show the parameter changes the deployment.

## Task 10: continuous testing and a deliberate defect
1. Run the pipeline with **INJECT_DEFECT = true**. The test container starts with `APP_DEMO_DEFECT=true`, search returns nothing, Selenium journeys J3 and J4 fail, the pipeline stops at *Selenium Tests*, and **Publish Image and Deploy never run**. Screenshot the red pipeline, the test report and the archived failure screenshots.
2. To make the defect a real code change instead (stronger evidence), edit `RecordController.list`, for example return an empty list for any query, commit as `test: introduce search defect`, push, and watch the build fail.
3. Fix: set the defect parameter back to false or revert the code with `fix(search): restore search results`, push, and run again. Screenshot the green run and the fix commit.

## Task 11: Docker lifecycle
```powershell
.\scripts\docker-lifecycle.ps1
```
This builds, tags, runs, inspects, stops, restarts and removes the container and writes `evidence\docker-lifecycle.log`. Add a screenshot of `docker ps` and the running app.

## Task 12: Jenkins to Docker Hub
1. Create a free Docker Hub account and an access token.
2. Jenkins, Credentials, add *Username with password* with id `dockerhub-creds` (username and the token as password).
3. Build with parameters: `PUSH_IMAGE=true`, `REGISTRY=docker.io/<your-username>`. The pipeline tags `irp-portal:1.0.<build>` and pushes both that tag and `latest`.
4. Screenshot the registry page listing the tag and the pipeline with all stages green. To use a local registry instead: `docker run -d -p 5000:5000 --name registry registry:2` and `REGISTRY=localhost:5000`.

## Task 13 and 14: Ansible (needs a Linux target)
Ansible does not run on a Windows control node. Use **WSL2 Ubuntu** (target and control in one: easiest), a VM, or a cloud instance.
```bash
# inside Ubuntu (WSL2): install tools
sudo apt update && sudo apt install -y ansible docker.io
sudo service docker start
cd /mnt/c/Users/<you>/path/to/institutional-repository-portal/ansible
ansible-galaxy collection install -r requirements.yml
```
Make the image available to the Linux Docker: either push it to the registry in Task 12 and set `image_repo: <user>/irp-portal`, or export and load it:
```powershell
docker save irp-portal:1.0.0 -o C:\temp\irp-portal.tar
```
then set `image_tar: /mnt/c/temp/irp-portal.tar`, `app_version: "1.0.0"`, `previous_version: "1.0.0"` in `group_vars/all.yml`.

**Task 13 first run**
```bash
ansible-playbook -i inventory.ini site.yml -K 2>&1 | tee ../evidence/task13-first-run.log
```
**Task 14**
```bash
# Idempotency: second run should show changed=0
ansible-playbook -i inventory.ini site.yml -K 2>&1 | tee ../evidence/task14-idempotent.log
curl -s http://localhost:8080/actuator/health                      # {"status":"UP"...}
# Rollback: ask for a version that does not exist; the playbook restores previous_version
ansible-playbook -i inventory.ini site.yml -e app_version=9.9.9 -K 2>&1 | tee ../evidence/task14-rollback.log
curl -s http://localhost:8080/api/info                             # still the previous version
```
The rollback run ends with a deliberate “failed” message so that automation notices; the log shows the rescue tasks and a healthy previous release.

## Troubleshooting
| Symptom | Fix |
|---|---|
| PowerShell says script is not digitally signed | `Set-ExecutionPolicy -Scope Process -ExecutionPolicy Bypass -Force` in the same window |
| Port 8080 already in use | Stop the other app, or `docker run -p 8085:8080 …` |
| Jenkins cannot find `docker` or `mvn` | Add them to the system PATH and restart the Jenkins service |
| Selenium cannot start Chrome | Install Chrome, or run in Jenkins where the Selenium container is used |
| Docker build is slow the first time | Maven downloads dependencies once; later builds use the layer cache |

## Quick look without installing anything
`demo/index.html` is the same interface with an in-browser stand-in for the API. Open it in any browser to rehearse the demo. It is **not** the real application: the real one is the Spring Boot, Tomcat and Docker build described above.
