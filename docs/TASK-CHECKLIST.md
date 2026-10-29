# Task checklist: what to submit for each of the 14 tasks

Tick each box when the file exists in `evidence/` (or the link is in your report).

| # | Task | Deliverables | Where it lives | Evidence file names |
|---|---|---|---|---|
| 1 | Problem definition and scope | Problem statement, stakeholders, objectives, constraints, MVP scope | `docs/01-problem-scope.md` | none needed |
| 2 | Agile planning and workflow | Backlog, task board, sprint plan, Definition of Done, lifecycle diagram | `docs/02-agile-plan.md` | `task02-kanban-board.png` |
| 3 | Requirements, architecture, setup | SRS summary, use cases, architecture, data model, API list, working local setup | `docs/03-architecture.md`, `README.md` | `task03-local-app.png`, `task03-mvn-verify.png` |
| 4 | Git and GitHub initialisation | Repo URL, README, .gitignore, templates, branch policy, initial commits | `.github/`, `docs/RUNBOOK.md` (Task 4) | `task04-repo.png`, `task04-git-log.png` |
| 5 | Feature branch and PR | Feature 1, branch, pull request, review comments, merge | GitHub | `task05-pr-review.png`, `task05-merge.png` |
| 6 | MVP completion and collaboration | Full MVP, second branch, conflict resolved, tag, updated backlog | `scripts/git-conflict-demo.ps1` | `task06-conflict.png`, `task06-resolved.png`, `task06-tag.png` |
| 7 | Jenkins CI job | Configured job, build log, trigger evidence, archived artefact | Jenkins | `task07-job-config.png`, `task07-build-log.png`, `task07-artifact.png`, `task07-trigger.png` |
| 8 | Pipeline as code and deploy | Jenkinsfile, successful run, deployed URL, parameter evidence | `Jenkinsfile` | `task08-pipeline-stages.png`, `task08-deployed-app.png`, `task08-parameters.png` |
| 9 | Selenium design and local run | Test plan, scripts, local report, failure screenshot mechanism | `selenium-tests/` | `task09-local-report.png`, `task09-failure-screenshot.png` |
| 10 | Continuous testing in Jenkins | Jenkins test report, failed run, defect fix commit, green rerun | `Jenkinsfile`, `selenium-tests/` | `task10-failed-run.png`, `task10-test-report.png`, `task10-fix-commit.png`, `task10-green-run.png` |
| 11 | Docker lifecycle | Dockerfile, image details, command log, running container | `Dockerfile`, `scripts/docker-lifecycle.ps1` | `evidence/docker-lifecycle.log`, `task11-container.png` |
| 12 | Jenkins to Docker CD | Versioned image, registry evidence, commit-to-container run | `Jenkinsfile` | `task12-registry.png`, `task12-pipeline.png` |
| 13 | Configuration management | Prerequisite spec, playbook, first run log | `ansible/`, table below | `task13-first-run.log` |
| 14 | Provisioning and reliability | Provisioned node, idempotency run, health check, rollback | `ansible/site.yml` | `task14-idempotent.log`, `task14-health.png`, `task14-rollback.log` |

## Task 13 configuration specification
| Item | Value |
|---|---|
| Packages | docker.io, python3-docker, curl |
| User and group | `irp` (system user, in `docker` group, no login shell) |
| Folders | `/opt/irp` (settings), `/var/lib/irp` (data volume) |
| Files | `/opt/irp/app.env` (environment, version, datasource) |
| Ports | 8080 on the host mapped to 8080 in the container |
| Services | `docker` enabled and started; container `irp-portal` with restart policy `unless-stopped` |

## Test plan for Task 9
| ID | Journey | Assertion |
|---|---|---|
| J1 | Dashboard loads | Heading is correct; total is at least 6; table has rows |
| J2 | Submitter creates record | New row is found by search with status Draft |
| J3 | Search | “crop” finds the seeded record; nonsense shows the empty message |
| J4 | Role workflow | Draft to Submitted to Approved to Published using three roles; no actions left |
| J5 | Visitor read-only | Save is disabled; no action or edit buttons |
Test data: seeded records from `V2__seed_records.sql` plus records created with a unique timestamped title. Failure screenshots go to `selenium-tests/target/screenshots/`.
