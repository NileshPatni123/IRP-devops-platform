# Presentation plan (7 minutes) and answers to hard questions

## The story (say this first, 30 seconds)
"Research done at an institution gets scattered across drives and shelves. Students cannot find it, nobody can tell whether it was reviewed, and the public cannot read work they often helped pay for. Our portal gives every record one home, a review trail before publication, and open search for everyone. The DevOps pipeline makes sure every change to it is built, tested and deployed the same safe way."
Link it to the goals shown on the home page: SDG 4 (education), SDG 9 (innovation), SDG 16.10 (public access to information).

## Demo order
| Time | Show | Say |
|---|---|---|
| 0:30 | Home page, impact tiles, charts | "Published records, publication rate, departments and what is waiting, all live." |
| 1:00 | Search `crop`, click a title, show workflow tracker and BibTeX | "Anyone can find and cite work." |
| 1:30 | Submitter creates a record, Submit; Reviewer Approves; Admin Publishes; then Visitor sees it read-only | "Nothing reaches the public without review." |
| 0:30 | Toggle dark mode, DevOps tab, live health | "Health is checked after every deployment." |
| 2:00 | Jenkins: run with `INJECT_DEFECT`, show *Selenium Tests* fail and no deploy; rerun green; `docker ps` | "A bad change cannot reach users." |
| 1:00 | Ansible run showing `changed=0`, then the rollback log | "Same result every run, and recovery to the last good version." |
| 0:30 | PostgreSQL: restart the stack, records still there | "Same code, production-style database." |

Rehearse once with the real stack. Keep `demo/index.html` open in a spare tab as a fallback: it is the same interface and works without any server.

## Before you go on stage
1. `docker compose -f docker-compose.postgres.yml up -d --build` and check http://localhost:8080.
2. Jenkins open, one green build already in history.
3. Screenshots of every task in `evidence/`.
4. Close other apps, set browser zoom to 110 percent.

## Hard questions
| Question | Honest answer |
|---|---|
| Is login real? | No. The role dropdown demonstrates the role-based workflow. A production version adds Spring Security with university single sign-on and database roles. We say this openly. |
| Who benefits? | Students and researchers who need findable work, faculty who review, librarians who publish, and the public, who can read published work. |
| How do you know it works? | Unit and API tests, five Selenium user journeys, a health check after deploy, and a demonstrated rollback. |
| What happens if a release is bad? | The pipeline stops on failing tests; if a deployed release fails its health check, Ansible restores the previous version. |
| Why PostgreSQL and H2 both? | H2 gives a zero-setup start and fast tests; PostgreSQL gives permanent data. The same Flyway migrations run on both. |
| What is not done? | File upload, real login, email notifications, full-text search, and standards such as OAI-PMH for harvesting by Google Scholar. These are the roadmap. |
| What would you build next? | Spring Security, file storage with virus scanning, OAI-PMH, SonarQube, Kubernetes. |

Claim only what you ran. If you did not execute the Ansible playbook, say "written and reviewed, not executed".
