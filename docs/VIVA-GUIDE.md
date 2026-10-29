# Viva guide
See also `docs/PRESENTATION-PLAN.md` (7-minute script) and `docs/SETUP-WINDOWS.md` (install and run everything).

## 5-minute demo order
1. **Product (1 min):** open the portal. Search “crop”. Click a title to show the detail dialog and BibTeX citation. Switch to the DevOps tab and show live health.
2. **Workflow (1 min):** as Submitter create a record, Submit it. Switch to Reviewer, Approve. Switch to Administrator, Publish. Show Visitor is read-only.
3. **Pipeline (2 min):** Jenkins run with `INJECT_DEFECT=true`: show it stopping at *Selenium Tests* with nothing deployed. Re-run without it: all green, container running.
4. **Operations (1 min):** show `docker ps`, the Ansible playbook, and the rollback log.

## Likely questions
| Question | Answer |
|---|---|
| Why Docker? | Same image everywhere, so “works on my machine” disappears; easy rollback by image tag. |
| Why did the pipeline stop? | Selenium journeys J3 and J4 failed, and stages after *Selenium Tests* only run on success. |
| What is idempotency? | Running the playbook again changes nothing when the node is already correct (`changed=0`). |
| How does rollback work? | The playbook tries the new version; if its health check fails, the `rescue` block restores `previous_version` and checks health again. |
| Is the role dropdown secure? | No. It demonstrates the role-based workflow. Production would use Spring Security with a login and database roles. |
| Why H2 and PostgreSQL? | H2 is the zero-setup default for tests and quick runs. PostgreSQL is supported now: `docker compose -f docker-compose.postgres.yml up -d --build`. The same Flyway migrations run on both. |
| Why a WAR on Tomcat? | The task requires a Tomcat or Nginx target; the image runs the WAR on Tomcat 10.1. |
| What would you add next? | Spring Security, PostgreSQL, SonarQube, Kubernetes manifests, blue-green deployment. |

## Be upfront about
Anything you did not run. “The playbook is written and reviewed but I did not execute it” is a safe answer; claiming a run you did not do is not.
