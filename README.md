<p align="center">
  <img src="assets/DevShell-LOGO.png" alt="DevShell Logo" width="120" />
</p>

<h1 align="center">DevShell — Personal Developer Operating System &amp; Terminal Analytics</h1>

<p align="center">
  <strong>A high-performance, terminal-first, local-first developer operating system and command center built with Java 21 LTS and Spring Boot.</strong>
</p>

<p align="center">
  <a href="https://www.npmjs.com/package/devshell"><img src="https://img.shields.io/npm/v/devshell.svg?style=flat-square&color=B7FF4A" alt="npm version" /></a>
  <a href="https://www.npmjs.com/package/devshell"><img src="https://img.shields.io/npm/dt/devshell.svg?style=flat-square&color=B7FF4A&label=total%20downloads" alt="total downloads" /></a>
  <a href="https://www.npmjs.com/package/devshell"><img src="https://img.shields.io/badge/npm%20downloads-2.1k%2B-B7FF4A?style=flat-square&logo=npm" alt="npm live downloads" /></a>
  <a href="https://www.oracle.com/java/"><img src="https://img.shields.io/badge/Java-21%20LTS-blue.svg?style=flat-square" alt="Java 21" /></a>
  <a href="https://spring.io/projects/spring-boot"><img src="https://img.shields.io/badge/Spring%20Boot-3.3.2-green.svg?style=flat-square" alt="Spring Boot" /></a>
  <a href="LICENSE"><img src="https://img.shields.io/badge/License-MIT-gray.svg?style=flat-square" alt="License" /></a>
</p>

---

<p align="center">
  <img src="assets/DevShell-BANNER.png" alt="DevShell Terminal Command Center & Analytics Banner" width="100%" />
</p>

---

## What is DevShell?

DevShell answers **"What did I actually do as a developer?"** rather than just "How many commits did I make?".

It builds a complete, privacy-respecting, local-first representation of your software engineering journey by analyzing:
- GitHub commits, line changes (`+add / -del`), pull requests, and code reviews
- Stack distribution (Backend, Mobile, Database, Frontend)
- Project health & maintenance activity states (`ACTIVE`, `MAINTAINED`, `LOW ACTIVITY`, `DORMANT`)
- Terminal contribution heatmaps & historical velocity trends (`--30d`, `--90d`, `--365d`)
- Personal development goals, developer level/XP progression, and daily focus recommendations
- Standalone HTML/Markdown/JSON exports for Developer Portfolio and Yearly Wrapped recaps

---

## Quick Start

### Requirements

For NPM installation:
- **Node.js 18+** & **NPM**

> 💡 **Zero Java Configuration**: DevShell automatically manages a compatible Java 21 LTS runtime locally (`~/.devshell/runtime/java-21`) when Java 21 is not already installed on your system. You do **not** need to manually install Java, set `JAVA_HOME`, or edit system `PATH`.

### Installation via npm

Install globally using Node Package Manager:

```bash
npm install -g devshell
```

Alternatively, run without installation using `npx`:

```bash
npx devshell
```

### Environment Verification

Verify your installation and runtime environment:

```bash
devshell doctor
```

### Execution

Launch DevShell from any command prompt or terminal:

```bash
devshell
```

*(Note: `devcli` is also supported as a command alias).*

---

## Core Command Reference

For comprehensive flags and details, see [`COMMANDS.md`](COMMANDS.md).

| Command | Category | Function |
| :--- | :--- | :--- |
| `devshell status` | **Daily OS** | Daily compact developer snapshot (Commits, Lines `+ / -`, Active Repos, Streak, Stack, Recent Projects) |
| `devshell dna` / `profile` | **Identity** | Categorized stack breakdown (Backend %, Mobile %, Database %, Frontend %) and productivity windows |
| `devshell trends [--30d\|90d\|365d]` | **Historical** | 30d/90d/365d trends with period-over-period percentage comparisons |
| `devshell diff` | **Comparison** | Period-over-period delta comparisons (Commits %, PR %, Active Projects, Learnings) |
| `devshell calendar` | **Heatmap** | Terminal ANSI contribution calendar grid (`░ ▒ ▓ █`) |
| `devshell projects` | **Health** | Classifies repos into `ACTIVE`, `MAINTAINED`, `LOW ACTIVITY`, `DORMANT` states |
| `devshell project <name>` | **Health** | Detailed project health metrics (Commits, Contributors, Open Issues, PRs, Last Activity) |
| `devshell goals` / `goal add` | **Goals** | Goal tracking with visual progress bars (`[██████████████░░] 82%`) |
| `devshell xp` | **Gamification** | Developer level and XP progression (+100 OS contribution, +50 PR merged, +30 Repo maintained) |
| `devshell radar` | **Attention** | Highlights inactive repos, open issues, pending PRs, and streaks |
| `devshell focus` | **Focus** | Data-driven daily focus recommendations |
| `devshell snapshot` | **Classifier** | Transparent developer health snapshot with explicit reasoning |
| `devshell history [--year]` | **Timeline** | Historical monthly development timeline |
| `devshell graph` | **Visual** | ASCII relationship tree (Project -> Stack -> Learnings -> Activity) |
| `devshell opensource` | **Open Source**| Journey tracker for public repos, external PRs, issues, and reviews |
| `devshell wrapped [--export html]`| **Recap** | Yearly recap presentation and standalone HTML report exporter |
| `devshell portfolio [--export html]`| **Portfolio** | Interactive developer portfolio generator with HTML export |
| `devshell report [weekly\|monthly]`| **Reporting** | Multi-format reporting engine (`terminal`, `markdown`, `json`, `html`) |
| `devshell learn [add\|view\|edit\|search\|pin]` | **Knowledge** | Personal Developer Knowledge Base (CRUD, tags, projects, pinning, search) |
| `devshell prompt` / `shell` | **Shell** | Shell prompt integration snippet generator for zsh/bash/fish |
| `devshell sync` | **Utility** | Immediate synchronization with GitHub API |
| `devshell --json` | **Scripting** | Emits pure machine-readable JSON output without ANSI escape codes |

---

## Local-First Philosophy & Privacy

DevShell operates on a strict **local-first** policy:
- Credentials and analytics reside strictly under `~/.devshell/*.json`.
- Direct HTTPS communications between your terminal and `api.github.com`.
- **Zero telemetry**, zero ad-tracking, zero third-party analytics collection.
- Your developer data never leaves your personal machine.

---

## License

This project is licensed under the **MIT License**. See the `LICENSE` file for details.