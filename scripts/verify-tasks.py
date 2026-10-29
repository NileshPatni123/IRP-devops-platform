"""Checks that every one of the 14 task deliverables exists in the project and contains its key content.
Run:  py scripts\\verify-tasks.py     (or python3 scripts/verify-tasks.py)
This proves the project contains the artefacts. Your evidence (screenshots, logs, GitHub, Jenkins runs) is separate."""
import pathlib, sys
R = pathlib.Path(__file__).resolve().parent.parent
def has(path, *needles):
    p = R / path
    if not p.exists(): return False
    t = p.read_text(encoding="utf-8", errors="ignore")
    return all(n in t for n in needles)
CHECKS = [
 (1, "Problem scope, stakeholders, objectives, MVP", lambda: has("docs/01-problem-scope.md", "Stakeholder", "Constraints", "MVP")),
 (2, "Backlog, board, sprint plan, DoD, lifecycle", lambda: has("docs/02-agile-plan.md", "Definition of Done", "flowchart", "Sprint 4")),
 (3, "SRS, use cases, architecture, data model, working setup", lambda: has("docs/03-architecture.md", "Use cases", "Data model") and has("pom.xml", "spring-boot-starter-web") and has("scripts/run-local.ps1", "mvn")),
 (4, "GitHub repo files, templates, branch policy", lambda: has(".gitignore", "target/") and has(".github/PULL_REQUEST_TEMPLATE.md", "Definition of Done") and has("docs/RUNBOOK.md", "Branch policy")),
 (5, "Feature branch + PR workflow", lambda: has("docs/RUNBOOK.md", "feature/12-record-search", "pull request")),
 (6, "MVP (create/view/update/search, workflow, dashboard), conflict, tag", lambda: has("src/main/java/edu/irp/portal/RecordController.java", "/records", "/dashboard", "/transition") and has("src/main/java/edu/irp/portal/WorkflowService.java", "REVIEWER") and has("scripts/git-conflict-demo.ps1", "git merge") and has("docs/RUNBOOK.md", "git tag -a v1.0.0")),
 (7, "Jenkins CI job: polling trigger, archive artefact", lambda: has("Jenkinsfile", "pollSCM", "archiveArtifacts") and has("docs/RUNBOOK.md", "Poll SCM")),
 (8, "Jenkinsfile checkout/build/package/deploy, Tomcat, parameters", lambda: has("Jenkinsfile", "stage('Checkout')", "stage('Deploy')", "ENVIRONMENT") and has("Dockerfile", "tomcat:10.1")),
 (9, "Selenium: 5 journeys, assertions, failure screenshots", lambda: has("selenium-tests/src/test/java/edu/irp/selenium/PortalJourneysTest.java", "J1", "J5") and has("selenium-tests/src/test/java/edu/irp/selenium/ScreenshotOnFailure.java", "getScreenshotAs")),
 (10, "Selenium in Jenkins; failed tests stop deploy; defect switch", lambda: has("Jenkinsfile", "Selenium Tests", "INJECT_DEFECT", "junit") and has("src/main/java/edu/irp/portal/RecordController.java", "demoDefect")),
 (11, "Dockerfile and container lifecycle script", lambda: has("Dockerfile", "FROM", "EXPOSE 8080") and has("scripts/docker-lifecycle.ps1", "docker stop", "docker restart", "docker rm")),
 (12, "Versioned image, registry push, commit-to-container", lambda: has("Jenkinsfile", "docker push", "VERSION", "docker run -d --name irp-portal")),
 (13, "Config spec + Ansible playbook + inventory", lambda: has("ansible/site.yml", "docker_container", "ansible.builtin.user") and has("ansible/inventory.ini", "portal") and has("docs/TASK-CHECKLIST.md", "configuration specification")),
 (14, "Provisioning, idempotency, health check, rollback", lambda: has("ansible/site.yml", "rescue:", "previous_version", "actuator/health") and has("docs/RUNBOOK.md", "idempotent", "rollback", "Rollback")),
]
bad = 0
for n, what, fn in CHECKS:
    ok = fn(); bad += (not ok)
    print(f"Task {n:>2}  {'PASS' if ok else 'MISSING'}  {what}")
print("\nAll 14 task deliverables are present." if not bad else f"\n{bad} task(s) incomplete.")
sys.exit(1 if bad else 0)
