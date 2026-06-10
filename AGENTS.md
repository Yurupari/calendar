# AGENTS.md — Guidance for automated coding/operational agents

Purpose: Give AI agents the minimal, actionable knowledge to build, run, and inspect the Calendar service.

Quick plan for agents
- Bring up infra (PostgreSQL, Prometheus, Grafana)
- Build or run a single service locally

Quick start (infra)
- From repo root: `docker-compose -f docker-compose-local.yaml up -d` (uses `docker-compose-local.yaml`)
- Stop: `docker-compose down`
- If DB issues occur: remove/recreate volumes or re-run `docker/postgres/init.sql`

Build / run a service
- Build: `./gradlew build`

Architecture & key components
- Stack: **Spring Boot 4** + **Java 25**. No Spring Cloud Config Server in this repo—config is per-service `application.yaml`.
- Important infra files: `docker-compose.yaml`, `docker-compose-local.yaml`, `docker/postgres/init.sql`.
- Human-oriented architecture diagrams: `diagrams/*.png` (see top-level `README.md`).

Critical integration points & dataflows (explicit)
- PostgreSQL: DB name `calendar`, init in `docker/postgres/init.sql`; JDBC URLs appear in `src/main/resources/application.yaml`.

Observability & useful endpoints
- Service port (defaults in `application.yaml`): **8080**
- Prometheus port (configuration in `docker/prometheus/prometheus.yml`): **9090**
- Grafana port (configuration folder in `docker/grafana/`): **3000**

Example agent actions (curl + checks)
- Post a test event (Kafka traffic; **direct to ingestion**, no JWT):
    - `curl -X POST http://localhost:8082/api/v1/user -H 'Content-Type: application/json' -d '{"name":"John","lastName":"Doe","email":"john.doe@example.com"}'`

Agent runbook checks (short)
- Confirm ports reachable: **5432** (PostgreSQL)
- Check service logs: `docker-compose logs <container>` or run the JAR locally and capture stdout

Project-specific conventions
- Gradle wrapper present in root folder — prefer `./gradlew`.
- Package names use underscores: e.g. `com.yurupari.calendar`.

Files to reference when automating (examples)
- `docker-compose.yaml` — infra and envs
- `docker-compose-local.yaml` — infra and envs for local development
- `docker/postgres/init.sql` — DB bootstrap
- `src/main/java/com/yurupari/calendar/controller/v1/UserControllerV1.java`

Testing
- Integration tests are in `src/test/java/com/yurupari/calendar/CalendarApplicationTests.java`
- The unit tests share the same routes as each of their classes

Notes
- Java version: use **JDK 25**.
- The top-level `README.md`.

# Problem context
## Backend Coding Challenge: Mini Doodle

The goal of the task is to create a mini Doodle. You should design and implement a high-performance simulation of a meeting scheduling platform using Spring Boot and Java technologies. The service should enable users to manage their time slots, schedule meetings, and view their custom calendar availability.

In this service, users should be able to define available slots, which can later be converted into meetings. Each user should have a personal calendar where their time is managed.

> **Domain Restriction:** *Calendar* as the term in the task should be present only in the domain in the service.

A slot can be booked as a meeting with a specific title and participants. The system should support querying free or busy slots, providing an aggregated view for a selected time frame. All data should be persisted to allow for proper management and querying.

---

### Functionalities to Implement

#### 1. Time Slot Management
* Allow users to create available time slots with configurable duration in calendars.
* Delete or modify existing time slots.
* Mark time slots as busy or free according to their availability.

#### 2. Meeting Scheduling
* Enable users to convert available slots into meetings.
* Add meeting details such as title, description, and participants.

#### Scale & Performance Constraint
Assume the platform may be used by hundreds of users with thousands of slots. Strive to design your solution according to that.

---

### Instructions

* **Version Control:** Create a new Git repository with an initial commit containing `README.md`. Develop your solution with regular, meaningful commits and share the repository link when complete.
* **Environment:** Your solution should be runnable locally using `docker-compose`. Don't forget to include all the dependencies of your service in the composer file.
* **API Usage:** Solution should document clearly how the service can be consumed.
* **Bonuses:** Metrics and documentation is a plus. Implementation of tests is a plus and something we would appreciate.

---

### Evaluation Focus
* The primary goal of the task is to see your design and tech decision making.
* Feel free to deliver a solution that is not completely finished, but make sure to explain your overall idea and architectural approach.

End of AGENTS.md