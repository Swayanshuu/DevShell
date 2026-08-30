# DevShell Command Reference

> Comprehensive documentation for all **DevShell** subcommands, flags, and usage options.

---

## Table of Contents

- [Primary Commands](#primary-commands)
  - [`devshell status`](#devshell-status)
  - [`devshell login`](#devshell-login)
  - [`devshell logout`](#devshell-logout)
- [Analytics & Developer DNA](#analytics--developer-dna)
  - [`devshell stats`](#devshell-stats)
  - [`devshell projects`](#devshell-projects)
  - [`devshell project <name>`](#devshell-project-name)
  - [`devshell activity`](#devshell-activity)
  - [`devshell insight`](#devshell-insight)
- [Milestones & Achievements](#milestones--achievements)
  - [`devshell achievements`](#devshell-achievements)
  - [`devshell timeline`](#devshell-timeline)
- [Developer Journal & Export](#developer-journal--export)
  - [`devshell learn`](#devshell-learn)
  - [`devshell bugs`](#devshell-bugs)
  - [`devshell export`](#devshell-export)
- [System Utilities & Flags](#system-utilities--flags)
  - [`devshell sync`](#devshell-sync)
  - [`devshell --help`](#devshell---help)
  - [`devshell --debug`](#devshell---debug)

---

## Primary Commands

### `devshell status`

Displays the primary command center dashboard snapshot. Executed by default when running `devshell` without subcommands.

**Output Data**:
- Authenticated user handle & profile status
- Daily contribution streak counter
- Commits pushed today
- Active project focus and recent activity log
- System observation summary

**Usage**:
```bash
devshell status
```

---

### `devshell login`

Authenticates DevShell with your GitHub account. Prompts an interactive terminal menu with options:
1. Browser OAuth authorization (`repo`, `read:user`, `user:email` scopes)
2. Personal Access Token (PAT) manual entry
3. Demo guest profile

Credentials are cached locally at `~/.devshell/credentials.json`.

**Usage**:
```bash
devshell login
```

---

### `devshell logout`

Revokes local authorization tokens and clears cached telemetry from `~/.devshell/`.

**Usage**:
```bash
devshell logout
```

---

## Analytics & Developer DNA

### `devshell stats`

Generates your Developer DNA Report.

**Output Data**:
- User profile summary (Name, Handle, GitHub User ID, Bio, Followers)
- Tech stack & language percentage distribution progress bars
- Contribution table (Total commits, active repos, PR reviews, streak evaluation)

**Usage**:
```bash
devshell stats
```

---

### `devshell projects`

Displays your GitHub repository universe categorized by status:
- **Active**: Commits within the last 14 days
- **Recently Active**: Commits within the last 30 days
- **Inactive**: No commits in over 30 days
- **Archived**: Repository is archived

**Usage**:
```bash
devshell projects
```

---

### `devshell project <name>`

Inspects telemetry for a specific repository.

**Usage**:
```bash
devshell project LinkPeer
devshell project LeetCode-Solutions
```

---

### `devshell activity`

Streams your chronological GitHub contribution feed.

**Flags**:
- `--today`: Filter activity for today only
- `--week`: Filter activity for the current week
- `--project <name>`: Filter activity for a specific project

**Usage**:
```bash
devshell activity
devshell activity --today
devshell activity --week
devshell activity --project LinkPeer
```

---

### `devshell insight`

Generates data-driven observations regarding coding habits, language specialization, and repository review velocity.

**Usage**:
```bash
devshell insight
```

---

## Milestones & Achievements

### `devshell achievements`

Displays unlocked milestone badges and contribution trackers.

**Milestone Badges**:
- **Commit Machine**: Push 100+ total commits
- **Ship It**: Maintain active repositories
- **Octopus**: Multi-language stack specialization
- **Builder**: Maintain 10+ projects
- **Consistent**: Maintain a 14+ day commit streak
- **Learner**: Record 5+ developer journal entries
- **Bug Hunter**: Resolve local bugs

**Usage**:
```bash
devshell achievements
```

---

### `devshell timeline`

Renders a visual commit frequency graph across recent weeks.

**Usage**:
```bash
devshell timeline
```

---

## Developer Journal & Export

### `devshell learn`

Log or view technical discoveries and architectural notes.

**Usage**:
```bash
# View all logged learnings:
devshell learn

# Record a new discovery:
devshell learn "Spring Boot WebClient timeout bounds configuration"
```

---

### `devshell bugs`

Local issue tracker for logging and resolving bugs prior to pushing code.

**Flags**:
- `--add "<description>"`: Log a new bug
- `--resolve <bug-id>`: Mark a bug as resolved

**Usage**:
```bash
# View open bugs:
devshell bugs

# Log a bug:
devshell bugs --add "NullPointer in WebSocket reconnect handler"

# Resolve a bug:
devshell bugs --resolve BUG-101
```

---

### `devshell export`

Exports a comprehensive Developer DNA report to a file.

**Flags**:
- `-f, --format <format>`: `markdown` (default), `json`, or `html`
- `-o, --output <file>`: Target file path (default: `devcli-report.md`)

**Usage**:
```bash
# Export as Markdown:
devshell export

# Export as JSON:
devshell export --format json

# Export as HTML:
devshell export --format html --output report.html
```

---

## System Utilities & Flags

### `devshell sync`

Forces an immediate data synchronization with the GitHub API.

**Usage**:
```bash
devshell sync
```

---

### `devshell --help`

Displays CLI options and interactive help screen.

**Usage**:
```bash
devshell --help
```

---

### `devshell --debug`

Enables verbose technical stack traces and debug output.

**Usage**:
```bash
devshell status --debug
```
