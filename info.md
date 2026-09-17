# DevShell Architecture, Tech Stack & Deep Dive Analysis

Welcome to the comprehensive technical documentation and learning guide for **DevShell** (`devshell` / `devcli`), a high-performance, local-first personal developer operating system and terminal analytics engine built by Swynx.

This document breaks down every layer of the codebase: tech stack choices, end-to-end execution flow, authentication security, GitHub API integration, background concurrency models, local storage engines, and domain analytics math.

---

## 📍 Table of Contents

1. [Executive Overview & Local-First Philosophy](#1-executive-overview--local-first-philosophy)
2. [Tech Stack Breakdown (What, Why, When, Where)](#2-tech-stack-breakdown-what-why-when-where)
3. [System Architecture & End-to-End Data Flow](#3-system-architecture--end-to-end-data-flow)
4. [Authentication Deep Dive](#4-authentication-deep-dive)
5. [GitHub API Integration & Data Processing Pipeline](#5-github-api-integration--data-processing-pipeline)
6. [Background Concurrency, Caching & Local Storage Engine](#6-background-concurrency-caching--local-storage-engine)
7. [Domain Analytics Engines & Calculation Logic](#7-domain-analytics-engines--calculation-logic)
8. [Summary & Key Takeaways for Developers](#8-summary--key-takeaways-for-developers)

---

## 1. Executive Overview & Local-First Philosophy

### What is DevShell?
DevShell is a command-line developer command center. Rather than just asking *"How many commits did I push?"*, DevShell answers:
- What is my actual stack breakdown (Backend, Mobile, Database, Frontend)?
- What is the operational health of my repositories (`ACTIVE`, `MAINTAINED`, `LOW ACTIVITY`, `DORMANT`)?
- What is my developer velocity and historical contribution heatmap across 30d/90d/365d?
- How close am I to my quarterly development goals and XP leveling progress?

### Local-First & Privacy Guarantee
DevShell operates under a strict **local-first** policy:
- **No intermediary backend server**: Communication happens directly via HTTPS between your local terminal and `api.github.com`.
- **Local Credentials & Cache**: Personal Access Tokens (PAT) and analytical data reside exclusively inside your home directory under `~/.devshell/*.json`.
- **Zero Telemetry**: No tracking cookies, no Google Analytics, no third-party telemetry, zero external data collection.

---

## 2. Tech Stack Breakdown (What, Why, When, Where)

DevShell employs a **hybrid architecture**: a lightweight Node.js wrapper acting as an orchestrator and package launcher, paired with a robust Java 21 LTS & Spring Boot core engine.

| Technology | Where Used | Why Chosen | When & How Invoked |
| :--- | :--- | :--- | :--- |
| **Node.js (CLI Wrapper)** | [`bin/devshell.js`](file:///c:/Shibu/Everything/Dev/devcli/bin/devshell.js) | Enables standard global installation via `npm install -g devshell` or `npx devshell` without forcing manual Java setup. | Triggered instantly when user runs `devshell` or `devcli` in terminal. |
| **Automated JDK Manager** | [`lib/launcher/*`](file:///c:/Shibu/Everything/Dev/devcli/lib/launcher) | Eliminates user friction. Automatically downloads and manages portable **Adoptium Temurin JDK 21 LTS** to `~/.devshell/runtime/java-21` if system Java 21 is missing. | Evaluated before application boot during runtime resolution phase. |
| **Java 21 LTS** | Core backend [`src/main/java`](file:///c:/Shibu/Everything/Dev/devcli/src/main/java) | High performance, virtual thread support, native HTTP/2 client (`java.net.http.HttpClient`), strong typing, cross-platform stability. | Launched by Node.js wrapper via `spawnSync`. |
| **Spring Boot 3.3.2** | Dependency Injection & Lifecycle | Clean separation of concerns using Spring components (`@Service`, `@Component`), automated bean wiring, lifecycle management. | Boots inside JVM process when running the application `.jar`. |
| **PicoCLI 4.7.5** | [`com.devcli.cli.*`](file:///c:/Shibu/Everything/Dev/devcli/src/main/java/com/devcli/cli) | Enterprise-grade command-line framework integrated with Spring Boot (`picocli-spring-boot-starter`). Manages options (`--json`, `--no-color`, `--debug`, `--theme`), subcommands, and ANSI formatting. | Routes CLI input arguments to appropriate command handlers. |
| **Jackson JSON Engine** | [`LocalStorageService.java`](file:///c:/Shibu/Everything/Dev/devcli/src/main/java/com/devcli/storage/LocalStorageService.java) | High-speed JSON serialization and deserialization with `JavaTimeModule` support for ISO date handling. | Used continuously for reading/writing `~/.devshell/*.json` local cache files and parsing GitHub REST payloads. |
| **LibrePDF / OpenPDF** | [`ReportExporter.java`](file:///c:/Shibu/Everything/Dev/devcli/src/main/java/com/devcli/service/ReportExporter.java) | Lightweight PDF rendering library for exporting visual developer summary reports. | Invoked when running `devshell report --export pdf` or `devshell portfolio`. |
| **GraalVM Native Image** | [`pom.xml`](file:///c:/Shibu/Everything/Dev/devcli/pom.xml) (`native` profile) | Compiles Java application into a standalone native binary executable (`devshell.exe`) with instantaneous startup time (<10ms). | Utilized for native binary distribution release builds. |

---

## 3. System Architecture & End-to-End Data Flow

The following diagram illustrates how a user command flows from terminal execution to visual output rendering:

```
[ User Terminal ]
       │
       ▼  `devshell status`
 ┌─────────────────────────────────────────────────────────────┐
 │ 1. Node.js Wrapper Launcher (bin/devshell.js)               │
 │    • Enforces Windows UTF-8 Code Page 65001                 │
 │    • Resolves/Downloads JDK 21 via runtime-manager.js        │
 │    • Compiles JAR via Maven if target JAR is missing        │
 └─────────────────────────────┬───────────────────────────────┘
                               │ `java -jar target/devshell-1.3.0.jar status`
                               ▼
 ┌─────────────────────────────────────────────────────────────┐
 │ 2. Spring Boot + PicoCLI Application Bootstrap              │
 │    • DevCliApplication main() configures UTF-8 Streams      │
 │    • Spring Context initializes Services & Storage Beans    │
 │    • Checks if `--help` or `login` command requested        │
 └─────────────────────────────┬───────────────────────────────┘
                               │
               ┌───────────────┴───────────────┐
               ▼                               ▼
 ┌───────────────────────────┐   ┌───────────────────────────┐
 │ 3a. Background Auto-Sync  │   │ 3b. Local Storage Load    │
 │     CompletableFuture     │   │     LocalStorageService   │
 │     SyncService.java      │   │     Loads cached JSON     │
 └─────────────┬─────────────┘   └─────────────┬─────────────┘
               │                               │
               ▼                               ▼
 ┌─────────────────────────────────────────────────────────────┐
 │ 4. Domain Engine Calculations                               │
 │    • DeveloperDnaEngine: Stack breakdown (Backend/Frontend)│
 │    • ProjectHealthEngine: Repository status (ACTIVE/DORMANT)│
 │    • TrendEngine: 30d/90d/365d growth trends                │
 │    • GoalEngine / XpEngine: Level & progress calculations   │
 └─────────────────────────────┬───────────────────────────────┘
                               │
                               ▼
 ┌─────────────────────────────────────────────────────────────┐
 │ 5. UI Rendering & Output Exporters                          │
 │    • AnsiStyle / BoxRenderer / TableRenderer / Spinner    │
 │    • Export engine: HTML / PDF / Markdown / Machine JSON   │
 └─────────────────────────────┬───────────────────────────────┘
                               │
                               ▼
 ┌─────────────────────────────────────────────────────────────┐
 │ 6. Background Update Check                                  │
 │    • UpdateCheckerService verifies NPM registry cache      │
 └─────────────────────────────────────────────────────────────┘
```

---

## 4. Authentication Deep Dive

Authentication in DevShell is handled by [`AuthService.java`](file:///c:/Shibu/Everything/Dev/devcli/src/main/java/com/devcli/service/AuthService.java).

```
User -> `devshell login` -> AuthService.java -> Opens Browser -> GitHub Token Page -> User enters PAT -> GitHub API Verification -> Saved to ~/.devshell/profile.json
```

### 1. GitHub Authorization Flow
When `devshell login` is executed:
1. DevShell constructs a pre-configured GitHub Personal Access Token (PAT) request URL:
   `https://github.com/settings/tokens/new?scopes=repo,read:user,user:email&description=DevShell+Command+Center`
2. **Automated Browser Launch**: Java's `Desktop.getDesktop().browse(URI)` automatically launches the system default browser directly to GitHub's token creation page.
3. Recommended Scopes:
   - `repo`: Access private/public repositories & commit histories.
   - `read:user`: Read user profile details and contribution history.
   - `user:email`: Match commits to account email address.

### 2. Token Validation & Profile Persistence
1. User pastes their generated token (`ghp_...` or `github_pat_...`) into the terminal prompt.
2. `GitHubService.fetchUserProfile(token)` sends an HTTP request to `https://api.github.com/user`.
3. Upon receiving HTTP 200 OK, DevShell constructs a `UserProfile` model containing:
   - GitHub User ID & Login Username
   - Avatar URL, Bio, Public & Private Repo Counts, Followers
   - Authentication Timestamp and Token
4. The profile is serialized to `~/.devshell/profile.json` using [`LocalStorageService.java`](file:///c:/Shibu/Everything/Dev/devcli/src/main/java/com/devcli/storage/LocalStorageService.java).

### 3. Logout & Credential Removal
Running `devshell logout` triggers `storageService.clearData()`, immediately deleting `profile.json`, `repositories.json`, `commits.json`, and `pull_requests.json` from the user's hard drive.

---

## 5. GitHub API Integration & Data Processing Pipeline

GitHub communication logic is encapsulated within [`GitHubService.java`](file:///c:/Shibu/Everything/Dev/devcli/src/main/java/com/devcli/service/GitHubService.java).

### 1. Modern HTTP Client Architecture
Instead of third-party heavy HTTP client dependencies, DevShell uses Java 21's native `java.net.http.HttpClient` with:
- Connection timeout set to 10 seconds.
- Standard headers: `Authorization: Bearer <token>`, `Accept: application/vnd.github.v3+json`, `User-Agent: DevCLI-App`.

### 2. GitHub REST Endpoints Utilized

| Data Requested | GitHub API Endpoint | Transformation & Processing |
| :--- | :--- | :--- |
| **User Profile** | `GET /user` | Validates token, extracts metadata, sets `UserProfile`. |
| **Repositories** | `GET /user/repos?sort=updated&per_page=50&affiliation=owner,collaborator` | Parses owner, stars, forks, primary language, default branch, archive flag, and pushed timestamps. |
| **Commits** | `GET /repos/{owner}/{repo}/commits?per_page=15` | Fetches recent commit SHAs, commit messages, author details, and timestamps for top active repos. |
| **Pull Requests** | `GET /search/issues?q=author:{username}+type:pr&sort=updated` | Pulls PR numbers, titles, states (`OPEN`, `CLOSED`, `MERGED`), repository links, created/closed dates. |
| **Issues** | `GET /search/issues?q=author:{username}+type:issue&sort=updated` | Pulls issue numbers, titles, states (`OPEN`, `CLOSED`), created/closed dates. |
| **Activity Events** | `GET /users/{username}/events?per_page=30` | Analyzes public user event stream (`PushEvent`, `CreateEvent`, `PullRequestEvent`, `WatchEvent`). |

---

## 6. Background Concurrency, Caching & Local Storage Engine

### 1. Asynchronous Auto-Sync Mechanism
To provide near-zero terminal latency, DevShell uses an asynchronous non-blocking sync strategy implemented in [`SyncService.java`](file:///c:/Shibu/Everything/Dev/devcli/src/main/java/com/devcli/service/SyncService.java) and [`DevCliApplication.java`](file:///c:/Shibu/Everything/Dev/devcli/src/main/java/com/devcli/DevCliApplication.java):

```java
// Launched during application startup in DevCliApplication.java:
CompletableFuture<Void> syncFuture = syncService.triggerAutoSync();

// Waits briefly up to 2 seconds for fresh data, or gracefully proceeds with cached data:
if (syncFuture != null) {
    try {
        syncFuture.get(2, TimeUnit.SECONDS);
    } catch (Exception ignored) {}
}
```

- **Cache-First Design**: Commands render instantaneously using existing local JSON cached data.
- **Background Fetch**: If internet is available, `CompletableFuture.runAsync()` syncs updated repos, commits, PRs, issues, and events without blocking terminal output.

### 2. Local File Storage Directory Structure (`~/.devshell/`)
All application data is isolated within the user's home directory:

```
~/.devshell/
├── profile.json            # Encrypted/Local GitHub authentication & user metadata
├── repositories.json       # Cached list of repositories with stats & classifications
├── commits.json            # Cached commit logs and authorship analytics
├── pull_requests.json      # Cached PR activity and merge metrics
├── issues.json             # Cached issues created or assigned
├── events.json             # Recent developer activity event logs
├── goals.json              # Personal quarterly goals & progress state
├── learnings.json          # Developer Knowledge Base entries (from `devshell learn`)
├── xp.json                 # Developer XP events & leveling log
├── snapshots.json          # Monthly historical snapshot trends
└── update.json             # NPM registry version check cache (4-hour refresh threshold)
```

---

## 7. Domain Analytics Engines & Calculation Logic

DevShell processes raw GitHub data through dedicated domain engines:

### 1. Developer DNA Engine (`DeveloperDnaEngine.java`)
Calculates stack distribution by categorizing repository languages and topics into core technology buckets:
- **Backend**: Java, Go, Rust, Python, C++, C#, Kotlin, Scala
- **Frontend**: TypeScript, JavaScript, HTML, CSS, Vue, React, Svelte
- **Mobile**: Swift, Flutter, Dart, Android Java, React Native
- **Database / Infrastructure**: SQL, PLpgSQL, HCL (Terraform), Dockerfile, Shell

$$\text{Stack Percentage} = \left( \frac{\text{Repositories in Category}}{\text{Total Categorized Repositories}} \right) \times 100\%$$

### 2. Project Health Engine (`ProjectHealthEngine.java`)
Classifies repositories based on recent commit velocity and activity timestamps:
- `ACTIVE`: Pushed commits within the last **14 days**.
- `MAINTAINED`: Pushed commits within the last **60 days**.
- `LOW ACTIVITY`: Pushed commits within the last **180 days**.
- `DORMANT`: No commit activity for over **180 days**.
- `ARCHIVED`: Explicitly archived on GitHub.

### 3. Gamification & Leveling Engine (`XpEngine.java`)
Calculates developer experience points (XP) and leveling progression:
- **+100 XP**: System activity / CLI session sync
- **+50 XP**: Pull request merged
- **+30 XP**: Repository maintained
- **+20 XP**: Goal completed

$$\text{Level} = \left\lfloor \frac{\text{Total XP}}{500} \right\rfloor + 1$$

---

## 8. Summary & Key Takeaways for Developers

1. **Clean Separation of Concerns**: Node.js launcher handles platform integration & zero-config Java runtime provisioning, while Spring Boot + PicoCLI in Java handles data analytics, API communication, and ANSI rendering.
2. **Local-First & Privacy Preserving**: Zero external cloud dependency. All tokens and analytics stay on the developer's machine in `~/.devshell/`.
3. **High-Performance Architecture**: Uses Java 21 native HTTP clients, Jackson JSON caching, and non-blocking `CompletableFuture` background synchronization to ensure commands run with minimal latency.
4. **Extensible Architecture**: Easy to inspect, customize, or build upon using standard Maven commands (`mvn clean package`).

---
*Created for DevShell — Personal Developer Operating System & Terminal Analytics by Swynx.*
