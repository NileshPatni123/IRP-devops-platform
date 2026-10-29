# Task 1: Problem definition and scope

## Problem statement
Research output at the institution (theses, journal papers, reports) is scattered across personal drives, department shelves and email. Students and staff cannot find it, nobody can tell whether a document was reviewed, and there is no single record of what has been published.

## Stakeholders
| Stakeholder | Interest |
|---|---|
| Students and researchers (submitters) | Deposit work once, track its review status |
| Faculty reviewers | See a queue of submissions; approve or reject |
| Librarian or repository administrator | Publish approved work, keep the catalogue consistent |
| Readers and visitors | Search and read published work |
| IT / DevOps team | Reliable, repeatable build, test and deployment |

## Pain points
Duplicate or lost files; no review trail; no search; manual and error-prone deployment of any tool built to fix this.

## Objectives and measurable success criteria
| Objective | Measure |
|---|---|
| Central catalogue | All seeded and new records are searchable by title, author, keyword or department |
| Controlled publication | A record reaches Published only through Draft, Submitted, Approved, with the right role at each step |
| Visibility | Dashboard shows counts per status |
| Repeatable delivery | Every commit builds, tests and produces a versioned Docker image with no manual steps |
| Quality gate | Failing unit or Selenium tests stop deployment (demonstrated once) |
| Recovery | A bad release is rolled back to the previous version and passes a health check |

## Constraints
Single-developer, course timeline; free tools only; Windows development machine; no production user directory (roles are simulated).

## Approved MVP scope (frozen)
**In:** create, view, edit, search records; role-based status workflow; summary dashboard; Maven build; Tomcat deployment; Docker image; Jenkins pipeline; Selenium tests; Ansible provisioning and rollback.
**Out:** file upload, real login and SSO, email notifications, external persistent database, full-text indexing.
