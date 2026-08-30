# DevShell — Developer Command Center & Terminal Analytics

> **A high-performance, terminal-based developer analytics engine and command center built with Java 21 and Spring Boot.**

[![npm version](https://img.shields.io/npm/v/devshell.svg?style=flat-square)](https://www.npmjs.com/package/devshell)
[![Java 21](https://img.shields.io/badge/Java-21-blue.svg?style=flat-square)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.2-green.svg?style=flat-square)](https://spring.io/projects/spring-boot)
[![License: MIT](https://img.shields.io/badge/License-MIT-gray.svg?style=flat-square)](LICENSE)

---

```text
╭──────────────────────────────────────────────────────────╮
│                                                          │
│   ██████╗ ███████╗██╗   ██╗███████╗██╗  ██╗███████╗██╗   │
│   ██╔══██╗██╔════╝██║   ██║██╔════╝██║  ██║██╔════╝██║   │
│   ██║  ██║█████╗  ██║   ██║███████╗███████║█████╗  ██║   │
│   ██║  ██║██╔══╝  ╚██╗ ██╔╝╚════██║██╔══██║██╔══╝  ██║   │
│   ██████╔╝███████╗ ╚████╔╝ ███████║██║  ██║███████╗█████╗│
│   ╚═════╝ ╚══════╝  ╚═══╝  ╚══════╝╚═╝  ╚═╝╚══════╝╚════╝│
│                                                          │
│     Your Personal Developer Command Center • Swynx       │
│                                                          │
╰──────────────────────────────────────────────────────────╯
```

**DevShell** is a local-first command-line application that aggregates your GitHub development activity, evaluates contribution metrics, tracks daily commit velocity, analyzes tech stack composition, and logs architectural discoveries directly within your terminal.

---

## Quick Start

### Installation via npm

Install globally using Node Package Manager:

```bash
npm install -g devshell
```

Alternatively, run without installation using `npx`:

```bash
npx devshell
```

### Execution

Once installed, launch DevShell from any command prompt or terminal:

```bash
devshell
```

*(Note: `devcli` is also supported as a command alias).*

---

## Core Capabilities

- **Command Center Dashboard (`devshell status`)**: Displays real-time commit streak tracking, today's contribution count, active building project, and recent repository activity.
- **Developer DNA Analytics (`devshell stats`)**: Generates comprehensive language distribution percentages, PR reviews, and contribution velocity metrics.
- **Repository Management (`devshell projects`)**: Automatically categorizes your GitHub repositories into Active, Recently Active, Inactive, and Archived states. Inspect individual repository telemetry using `devshell project <name>`.
- **Developer Journal (`devshell learn`)**: Log architectural insights, stack learnings, and technical notes directly from the command line.
- **Local Issue Tracker (`devshell bugs`)**: Track and resolve local bugs prior to pushing code to remote repositories.
- **Report Export (`devshell export`)**: Export your developer profile and activity metrics to Markdown, JSON, or HTML formats.
- **Local-First Architecture**: Session credentials and cached telemetry reside locally at `~/.devshell/credentials.json` with zero external tracking.

---

## Command Reference

For comprehensive flags and options, refer to [`COMMANDS.md`](COMMANDS.md).

| Command | Function | Example |
| :--- | :--- | :--- |
| `devshell status` | Displays main dashboard snapshot (default command) | `devshell status` |
| `devshell login` | Authenticates with GitHub via OAuth browser flow or PAT | `devshell login` |
| `devshell logout` | Revokes stored credentials and clears local cache | `devshell logout` |
| `devshell stats` | Generates developer DNA report and stack breakdown | `devshell stats` |
| `devshell projects` | Lists categorized repository universe | `devshell projects` |
| `devshell project <name>` | Inspects telemetry for a specific repository | `devshell project LinkPeer` |
| `devshell activity` | Displays chronological commit and review feed | `devshell activity --today` |
| `devshell achievements` | Displays unlocked developer milestones | `devshell achievements` |
| `devshell insight` | Evaluates development patterns and code health | `devshell insight` |
| `devshell learn "<note>"` | Records a technical discovery or architectural note | `devshell learn "Spring WebClient timeout"` |
| `devshell bugs` | Manages local issue log (`--add`, `--resolve`) | `devshell bugs --add "Fix NPE"` |
| `devshell export` | Exports profile report (`--format md\|json\|html`) | `devshell export --format markdown` |
| `devshell sync` | Forces immediate synchronization with GitHub API | `devshell sync` |
| `devshell --help` | Prints CLI usage guide and options | `devshell --help` |

---

## Architecture & Tech Stack

- **Language Runtime**: Java 21 LTS
- **Application Framework**: Spring Boot 3.3.2
- **Command Line Parser**: Picocli 4.7
- **Terminal Layout Engine**: Custom ANSI Box & Progress Bar Renderer (Code Page 65001 / UTF-8)
- **Package Distribution**: npm (`bin/devshell.js`)
- **Data Persistence**: Local JSON Storage Engine (`~/.devshell/`)

---

## Security & Privacy

DevShell operates on a strict **local-first** policy:
- Credentials are encrypted and saved locally in `~/.devshell/credentials.json`.
- API calls are executed directly between your local terminal and `api.github.com`.
- No telemetry, analytics, or source code data is ever collected or sent to secondary servers.

---

## License

This project is licensed under the **MIT License**. See the `LICENSE` file for details.