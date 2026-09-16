# DevShell Command Reference

> Comprehensive documentation for all **DevShell** Personal Developer OS subcommands, flags, and options.

---

## Table of Contents

- [Primary OS Commands](#primary-os-commands)
  - [`devshell status`](#devshell-status)
  - [`devshell dna` / `devshell profile`](#devshell-dna--devshell-profile)
  - [`devshell trends`](#devshell-trends)
  - [`devshell diff`](#devshell-diff)
  - [`devshell calendar`](#devshell-calendar)
- [Project Health & Analytics](#project-health--analytics)
  - [`devshell projects`](#devshell-projects)
  - [`devshell project <name>`](#devshell-project-name)
  - [`devshell radar`](#devshell-radar)
  - [`devshell focus`](#devshell-focus)
  - [`devshell snapshot`](#devshell-snapshot)
  - [`devshell graph`](#devshell-graph)
- [Goals, XP & Milestones](#goals-xp--milestones)
  - [`devshell goals` / `devshell goal add`](#devshell-goals--devshell-goal-add)
  - [`devshell xp`](#devshell-xp)
  - [`devshell achievements`](#devshell-achievements)
  - [`devshell history`](#devshell-history)
- [Open Source, Portfolio & Wrapped](#open-source-portfolio--wrapped)
  - [`devshell opensource`](#devshell-opensource)
  - [`devshell wrapped`](#devshell-wrapped)
  - [`devshell portfolio`](#devshell-portfolio)
  - [`devshell report`](#devshell-report)
- [Developer Knowledge Base & Utilities](#developer-knowledge-base--utilities)
  - [`devshell learn`](#devshell-learn)
  - [`devshell bugs`](#devshell-bugs)
  - [`devshell prompt`](#devshell-prompt)
  - [`devshell sync`](#devshell-sync)
- [Global Scripting Flags](#global-scripting-flags)
  - [`--json`](#--json)
  - [`--no-color`](#--no-color)
  - [`--quiet`](#--quiet)

---

## Primary OS Commands

### `devshell status`

Displays the primary daily developer snapshot. Executed by default when running `devshell`.

```bash
DEV SHELL
────────────────────────────────────────

TODAY

  Commits              7
  Lines changed        +842 / -231
  Active repositories  3
  Pull requests        2
  Reviews              1

STREAK

  Current              12 days
  Longest              27 days

TOP STACK

  Java                 42%
  Dart                 28%
  SQL                  14%

RECENT PROJECTS

  LinkPeer             2h ago
  DevShell             5h ago
  BatchMate            1d ago
```

---

### `devshell dna` / `devshell profile`

Generates categorized developer identity analytics:
- Stack breakdown (Backend %, Mobile %, Database %, Frontend %, Other %)
- Primary Domain identification
- Peak productivity time window (`12 PM - 5 PM`, `8 PM - 12 AM`)
- Streak history, public repositories, and pull requests.

```bash
devshell dna
devshell profile
```

---

### `devshell trends`

Tracks historical development trends over 30d, 90d, or 365d windows with period-over-period percentage comparisons.

```bash
devshell trends --30d
devshell trends --90d
devshell trends --365d
```

---

### `devshell diff`

Compares current and previous activity periods for commits, PR activity, active projects, and learnings.

```bash
devshell diff
```

---

### `devshell calendar`

Renders a terminal ANSI contribution heatmap grid (`░ ▒ ▓ █`) with color legend.

```bash
devshell calendar
```

---

## Project Health & Analytics

### `devshell projects`

Lists your repository universe categorized by activity state:
- **`ACTIVE`**: Commits within last 14 days
- **`MAINTAINED`**: Commits within last 60 days
- **`LOW ACTIVITY`**: Commits within last 180 days
- **`DORMANT`**: No commits in over 180 days

```bash
devshell projects
```

---

### `devshell project <name>`

Displays detailed project health dashboard (Commits, Contributors, Open Issues, PRs, Last Activity, Activity state, Maintenance status, Languages).

```bash
devshell project DevShell
devshell project LinkPeer
```

---

### `devshell radar`

Attention tracker highlighting inactive repositories, open issues, pending PRs, and streaks.

```bash
devshell radar
```

---

### `devshell focus`

Provides data-driven daily focus recommendations based on your active repositories, issues, and learnings.

```bash
devshell focus
```

---

### `devshell snapshot`

Evaluates developer activity snapshot with explicit reasoning for Consistency, Project Activity, Open Source, Documentation, Maintenance, and Learning.

```bash
devshell snapshot
```

---

### `devshell graph`

Renders an ASCII relationship tree (Project -> Stack -> Learnings -> Activity).

```bash
devshell graph
```

---

## Goals, XP & Milestones

### `devshell goals` / `devshell goal add`

Creates and tracks personal developer goals with visual progress bars (`[██████████████░░] 82%`).

```bash
# View goals:
devshell goals

# Add a goal:
devshell goal add "Solve 100 LeetCode problems"
```

---

### `devshell xp`

Displays developer level, XP progression bar, weekly gained XP, and breakdown (+100 Open source contribution, +50 PR merged, +30 Repo maintained, +20 Learning, +10 Daily activity).

```bash
devshell xp
```

---

### `devshell achievements`

Displays unlocked developer milestone badges.

```bash
devshell achievements
```

---

### `devshell history`

Renders a monthly development timeline history.

```bash
devshell history
devshell history --year 2026
```

---

## Open Source, Portfolio & Wrapped

### `devshell opensource`

Tracks your open-source journey, public repos, external contributions, PRs, issues, reviews, and merged PRs.

```bash
devshell opensource
```

---

### `devshell wrapped`

Generates a yearly development recap presentation. Use `--export html` to generate a standalone HTML report.

```bash
devshell wrapped
devshell wrapped --export html
```

---

### `devshell portfolio`

Generates an interactive developer portfolio. Use `--export html` to export a standalone HTML webpage.

```bash
devshell portfolio
devshell portfolio --export html
```

---

### `devshell report`

Generates multi-format development reports.

```bash
devshell report weekly --format terminal
devshell report monthly --format markdown
devshell report yearly --format json
```

---

## Developer Knowledge Base & Utilities

### `devshell learn`

Terminal-first Personal Developer Knowledge Base to record, edit, pin, and search technical learnings.

```bash
# List all recorded learnings (pinned & recent):
devshell learn

# Direct shortcut to record a note:
devshell learn "Spring Security filter chain order"

# Add a learning with full metadata:
devshell learn add "Implemented JWT token refresh flow" --title "JWT Refresh Flow" --tags "spring,security" --project "LinkPeer" --category "Backend"

# View full learning details:
devshell learn view LEARN-001

# Edit an existing learning (interactive or via flags):
devshell learn edit LEARN-001 --title "Updated Title" --tags "spring,security,auth"

# Pin or unpin important notes:
devshell learn pin LEARN-001
devshell learn unpin LEARN-001

# Search learnings by text query, tag, or project:
devshell learn search "spring"
devshell learn search --tag security
devshell learn search --project LinkPeer

# Delete a learning entry:
devshell learn delete LEARN-001 --yes

# Export Knowledge Base as JSON:
devshell learn --json
```

---

### `devshell bugs`

Local issue tracker for pre-commit bug tracking.

```bash
devshell bugs
devshell bugs --add "Fix NPE in WebSocket reconnect"
devshell bugs --resolve BUG-101
```

---

### `devshell prompt`

Generates shell prompt integration snippets for `zsh`, `bash`, and `fish`.

```bash
devshell prompt
```

---

### `devshell sync`

Triggers an immediate synchronization with the GitHub REST API.

```bash
devshell sync
```

---

## Global Scripting Flags

Every major command supports global output options for scripting and CI pipelines:

- `--json`: Emits pure machine-readable JSON without ANSI escape codes.
- `--no-color`: Disables terminal ANSI color formatting.
- `--quiet`: Suppresses non-essential console logs.

```bash
devshell status --json
devshell trends --no-color
```
