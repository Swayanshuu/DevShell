# DevShell WRAP — Development Plan

## 1. What We Are Building

Add a new:

```bash
devshell wrap
```

feature that generates a **1080×1920 (9:16) developer statistics image**.

The basic concept:

```text
DevShell GitHub Data
        ↓
Calculate WRAP Statistics
        ↓
Select Theme
        ↓
Load Theme Background
        ↓
Render Dynamic Text
        ↓
Generate PNG
```

The **background/design is provided as an image**.

DevShell only renders the user's real statistics on top of that image.

---

## 2. Example User Experience

```bash
devshell wrap
```

Output:

```text
Generating Developer Wrap...

Theme: DevShell
Period: 2026

✓ Statistics collected
✓ Template loaded
✓ Image rendered

Generated:
./devshell-wrap-2026.png
```

The generated image can contain:

```text
DEV WRAP
2026

1,842
COMMITS

37
PROJECTS

14.2K
LINES ADDED

128
PULL REQUESTS

JAVA
TOP LANGUAGE

DEVSHELL
TOP PROJECT
```

The exact visual layout depends on the selected theme.

---

# 3. Core Architecture

The most important architectural principle is:

> **Separate WRAP data from WRAP design.**

```text
                    WRAP
                     │
          ┌──────────┴──────────┐
          │                     │
        DATA                  THEME
          │                     │
   GitHub analytics       Background image
          │               Layout configuration
          │               Typography
          │                     │
          └──────────┬──────────┘
                     ↓
                 RENDERER
                     ↓
                 PNG IMAGE
```

The analytics system produces the data.

The theme determines how the data is displayed.

The renderer combines both.

---

# 4. Themes

Initially support three themes:

```text
devshell
batman
spiderman
```

The actual background images will be provided separately.

Target structure:

```text
src/main/resources/
└── wrap/
    └── themes/
        ├── devshell/
        │   ├── background.png
        │   └── layout.json
        │
        ├── batman/
        │   ├── background.png
        │   └── layout.json
        │
        └── spiderman/
            ├── background.png
            └── layout.json
```

Each theme can have a completely different visual layout.

---

# 5. Background Images

Each theme background must be:

```text
Width: 1080px
Height: 1920px
Aspect Ratio: 9:16
Format: PNG
```

The background should contain:

- artwork
- visual elements
- decorative elements
- theme-specific composition
- intentional negative space for statistics

The background should **not** contain:

- statistics
- dynamic numbers
- usernames
- commit counts
- project names
- generated labels
- dynamic DevShell data

DevShell will add those later.

---

# 6. `layout.json`

Every theme needs a layout configuration.

The purpose is to tell DevShell:

> Where should each dynamic element be rendered?

Example:

```json
{
  "width": 1080,
  "height": 1920,

  "elements": {
    "title": {
      "type": "text",
      "x": 540,
      "y": 180,
      "align": "CENTER",
      "fontSize": 64
    },

    "year": {
      "type": "text",
      "x": 540,
      "y": 280,
      "align": "CENTER",
      "fontSize": 100
    },

    "commits": {
      "type": "stat",
      "x": 160,
      "y": 600,
      "align": "CENTER",
      "valueSize": 90,
      "labelSize": 24
    }
  }
}
```

This means the renderer does not need theme-specific hardcoded coordinates.

---

# 7. Rendering Technology

Use Java's native image APIs.

Primary technologies:

```text
Java 21
BufferedImage
Graphics2D
Font
FontMetrics
ImageIO
```

### `BufferedImage`

Loads and represents the background image.

### `Graphics2D`

Draws:

- text
- lines
- shapes
- icons
- overlays
- other visual elements

### `Font`

Controls typography.

### `FontMetrics`

Handles accurate:

- text width
- centering
- alignment
- positioning

### `ImageIO`

Exports the final image as PNG.

No AI image generation will be involved in the final rendering.

---

# 8. WRAP Data Model

Create a dedicated model for the data used by the renderer.

Initial fields:

```text
Period
Commits
Pull Requests
Reviews
Lines Added
Lines Deleted
Projects
Active Days
Longest Streak
Top Language
Top Repository
```

Example:

```json
{
  "period": "2026",
  "commits": 1842,
  "pullRequests": 128,
  "reviews": 64,
  "additions": 14280,
  "deletions": 9420,
  "projects": 37,
  "activeDays": 184,
  "longestStreak": 42,
  "topLanguage": "Java",
  "topRepository": "DevShell"
}
```

These values must eventually come from **existing DevShell analytics/history systems**.

Do not create a second GitHub analytics system specifically for WRAP.

---

# 9. Rendering Pipeline

The complete rendering pipeline will be:

```text
devshell wrap
      ↓
WrapCommand
      ↓
WrapService
      ↓
Collect existing DevShell statistics
      ↓
Create WrapData
      ↓
ThemeManager
      ↓
Load background.png
      ↓
Load layout.json
      ↓
WrapRenderer
      ↓
Graphics2D
      ↓
Render dynamic text
      ↓
ImageIO
      ↓
PNG
```

---

# 10. Planned Java Components

The eventual structure can be approximately:

```text
wrap/
├── WrapCommand.java
├── WrapService.java
├── WrapData.java
├── WrapRenderer.java
├── WrapTheme.java
├── WrapThemeManager.java
├── WrapLayout.java
└── WrapExporter.java
```

The exact structure should be adjusted after inspecting the existing DevShell architecture.

Do not create unnecessary abstractions.

---

# 11. First Version

The first implementation should be extremely small.

Only support:

```bash
devshell wrap
```

It should:

1. Use one theme.
2. Use one background.
3. Use temporary test data.
4. Render the data onto the image.
5. Export a PNG.

Example:

```text
background.png
      +
test statistics
      ↓
WrapRenderer
      ↓
devshell-wrap.png
```

At this stage, do **not** implement every theme or every statistic.

---

# 12. Connect Real DevShell Data

Once the renderer works correctly, replace temporary values with existing DevShell analytics.

For example:

```text
Existing GitHub Analytics
        ↓
WrapService
        ↓
WrapData
        ↓
WrapRenderer
```

Reuse existing systems for:

- commits
- repositories
- pull requests
- reviews
- languages
- streaks
- activity
- history

Do not duplicate existing functionality.

---

# 13. Period Support

After the first version works, add:

```bash
devshell wrap --week
```

```bash
devshell wrap --month
```

```bash
devshell wrap --year
```

The renderer should remain unchanged.

Only the data collection period changes:

```text
Week
 ↓
WrapData

Month
 ↓
WrapData

Year
 ↓
WrapData

      ↓

Same WrapRenderer
```

---

# 14. Theme Support

After one theme works:

```bash
devshell wrap --year --theme devshell
```

Then:

```bash
devshell wrap --year --theme batman
```

And:

```bash
devshell wrap --year --theme spiderman
```

The same renderer handles all themes.

Only these change:

```text
background.png
layout.json
```

---

# 15. Output

Default output:

```text
./devshell-wrap-2026.png
```

Later support:

```bash
devshell wrap --output ~/Desktop/my-wrap.png
```

Potential future option:

```bash
devshell wrap --open
```

to automatically open the generated image.

---

# 16. Dynamic Elements

The renderer should eventually support reusable elements such as:

```text
TextElement
StatElement
LabelElement
ImageElement
IconElement
BrandElement
```

Example configuration:

```json
{
  "type": "stat",
  "metric": "commits",
  "x": 120,
  "y": 600,
  "valueSize": 90,
  "labelSize": 22
}
```

The renderer interprets:

```text
metric = commits
       ↓
WrapData.commits
       ↓
Render value
```

This makes themes much easier to maintain.

---

# 17. Statistics

Potential WRAP statistics:

```text
Commits
Pull Requests
Code Reviews
Lines Added
Lines Deleted
Active Projects
Active Days
Longest Streak
Top Language
Top Repository
Most Active Month
Most Active Day
Languages Used
Repositories Contributed To
Open Source Contributions
```

Not every theme needs to display every statistic.

The layout decides which statistics are visible.

---

# 18. Accuracy Rule

WRAP must never invent statistics.

Everything displayed should come from actual DevShell data.

For example:

```text
1,842 commits
```

must represent actual collected activity.

Avoid unsupported subjective claims such as:

```text
"You were extremely productive."
"You're becoming a better engineer."
"You're a 10x developer."
```

WRAP should primarily be **data-driven**.

---

# 19. Existing DevShell Systems Must Be Reused

DevShell already contains:

```text
GitHub API
Analytics
Projects
Activity
History
Languages
Streaks
Reports
```

WRAP should sit on top of these systems.

Do not create:

```text
New GitHub client
New authentication system
New analytics engine
New storage system
```

just for WRAP.

WRAP is primarily a **presentation/export layer**.

---

# 20. Target Project Structure

Eventually the project should look approximately like:

```text
src/
├── main/
│   ├── java/
│   │   └── ...
│   │       └── wrap/
│   │           ├── WrapCommand.java
│   │           ├── WrapService.java
│   │           ├── WrapData.java
│   │           ├── WrapRenderer.java
│   │           ├── WrapTheme.java
│   │           ├── WrapThemeManager.java
│   │           ├── WrapLayout.java
│   │           └── WrapExporter.java
│   │
│   └── resources/
│       └── wrap/
│           └── themes/
│               ├── devshell/
│               │   ├── background.png
│               │   └── layout.json
│               │
│               ├── batman/
│               │   ├── background.png
│               │   └── layout.json
│               │
│               └── spiderman/
│                   ├── background.png
│                   └── layout.json
```

This is the target architecture, not something that should all be created immediately.

---

# 21. Development Phases

## Phase 1 — Renderer Prototype

```text
One background
+
Hardcoded test data
+
Graphics2D
=
PNG
```

## Phase 2 — Theme Layout

```text
background.png
+
layout.json
=
Configurable rendering
```

## Phase 3 — Real DevShell Data

```text
Existing analytics
+
WrapData
=
Real statistics
```

## Phase 4 — Picocli Command

```bash
devshell wrap
```

## Phase 5 — Periods

```bash
--week
--month
--year
```

## Phase 6 — Multiple Themes

```bash
--theme devshell
--theme batman
--theme spiderman
```

## Phase 7 — Export Options

```bash
--output
--open
```

## Phase 8 — Polish

```text
Typography
Alignment
Number formatting
Error handling
Tests
README
COMMANDS.md
```

---

# 22. Important Development Rule

We will **not build everything at once**.

We will work step-by-step.

### Step 1

Inspect the actual DevShell project structure.

### Step 2

Create the first theme directory.

### Step 3

Add one `background.png`.

### Step 4

Create the simplest possible renderer.

### Step 5

Render hardcoded test text.

### Step 6

Generate the first PNG.

### Step 7

Verify that image positioning and quality are correct.

### Step 8

Only then move to real DevShell statistics.

---

# 23. What Happens Before Coding

Before writing WRAP code, provide the **actual DevShell project/file structure**.

For example:

```text
DevShell/
├── src/
├── pom.xml
├── package.json
├── bin/
├── README.md
├── COMMANDS.md
└── ...
```

After inspecting that structure, begin with **Phase 1 only**:

> **Create the first WRAP background and render test text onto it.**

Do not jump ahead to themes, GitHub integration, advanced statistics, or extra commands until the first rendering pipeline works correctly.
