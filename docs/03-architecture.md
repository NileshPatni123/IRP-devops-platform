# Task 3: Requirements, architecture and technology

## SRS summary
**Functional:** create, view, edit and search records; role-based workflow Draft, Submitted, Approved or Rejected, Published; dashboard with counts by status and department.
**Non-functional:** starts in under a minute; health endpoint; reproducible build; deployable to Tomcat and as a container; configuration through environment variables; no secrets in the repository.

## Technology choices
| Concern | Choice | Reason |
|---|---|---|
| Language and framework | Java 17, Spring Boot 3.3 | Standard for Tomcat WAR deployment |
| Build | Maven | Required by tasks; used by Jenkins |
| Database | H2 (file in container, in memory locally) with Flyway migrations | Zero setup; schema versioned |
| Deployment target | Tomcat 10.1 (WAR as ROOT) | Task 8 |
| UI | Static HTML and JavaScript served by the app | No separate front-end build |
| Tests | JUnit 5, MockMvc, Selenium 4 | Tasks 9 and 10 |
| CI/CD | Jenkins, Docker | Tasks 7, 8, 12 |
| Configuration management | Ansible | Tasks 13, 14 |

## Use cases
```mermaid
flowchart LR
  V([Visitor]) --- UC1(Search and view records)
  S([Submitter]) --- UC2(Create and edit record)
  S --- UC3(Submit for review)
  R([Reviewer]) --- UC4(Approve or reject)
  A([Administrator]) --- UC5(Publish)
  S --- UC1
  R --- UC1
  A --- UC1
  A --- UC6(View dashboard)
```

## Architecture
```mermaid
flowchart LR
  Browser --> Tomcat
  subgraph Container
    Tomcat --> App[Spring Boot app: REST API and static UI]
    App --> Flyway --> H2[(H2 database in /data volume)]
  end
  Jenkins -->|build, test, deploy| Container
  Ansible -->|configures host, runs container| Container
```

## Data model
`repository_records(id, title, authors, abstract_text, record_type, department, publication_year, keywords, status, handle_id, version_no, created_at, updated_at)`

## Status workflow
```mermaid
stateDiagram-v2
  [*] --> DRAFT: Submitter creates
  DRAFT --> SUBMITTED: Submitter or Admin
  SUBMITTED --> APPROVED: Reviewer or Admin
  SUBMITTED --> REJECTED: Reviewer or Admin
  REJECTED --> DRAFT: Submitter or Admin
  APPROVED --> PUBLISHED: Admin
```
The API table is in the [README](../README.md#api-summary).
