[اللغة العربية (AR)](./about_ar.md) |
[Azərbaycan dili (AZ)](./about_az.md) |
[Беларуская (BE)](./about_be.md) |
[Čeština (CS)](./about_cs.md) |
[Deutsch (DE)](./about_de.md) |
English (EN-US) |
[Español (ES)](./about_es.md) |
[فارسی (FA)](./about_fa.md) |
[Français (FR)](./about_fr.md) |
[हिंदी (HI-IN)](./about_hi.md) |
[Bahasa Indonesia (ID)](./about_id.md) |
[Italiano (IT)](./about_it.md) |
[日本語 (JA)](./about_ja.md) |
[ქართული (KA-GE)](./about_ka.md) |
[한국어 (KO)](./about_ko.md) |
[Nederlands (NL)](./about_nl.md) |
[Polski (PL)](./about_pl.md) |
[Português Brasileiro (PT-BR)](./about_pt.md) |
[Limba română (RO)](./about_ro.md) |
[Русский (RU)](./about_ru.md) |
[Türkçe (TR)](./about_tr.md) |
[Українська (UK-UA)](./about_uk.md) |
[Oʻzbekcha (UZ)](./about_uz.md) |
[简体中文 (ZH-CN)](./about_zh.md) |
[繁體中文 (ZH-TW)](./about_zh_TW.md) |

---

Moto: Small steps. Visible progress. Lasting habits.

Keywords: pomodoro,reminder,planner,calendar,focus,discipline,selfcare,todo,checklist,progress,motivation

**Zwyka** -- open-source cross-platform habits tracking application without Ads and limitations.
The goal of the solution is to create a habits tracking application that is intuitive, efficient, and inclusive.
That empowers users to effortlessly manage their habits while ensuring that no one is left behind.

### Functionality
- Habits
  - Title, remarks, icon (2,000+ Material icons), and color (palette or custom RGB / HEX)
  - Active days of the week; inactive days are marked with "—"
  - Archive (deactivate) a habit: it is grayed out, shown last, and hidden in the Schedule
  - Numeric target, e.g. 10000 steps: a day keeps the reached value, completion is value / target
  - Habit to avoid (set on creation): the icon is crossed out, a day without a mark is 100%, a mark gives 100% minus its percentage, Day Streak counts the days without marks
- Habits Calendar (main page)
  - Today: today's habits as a list, the completed and skipped ones go below; swipe right marks a habit as done (a numeric target asks for its value), swipe left skips it, a tap opens the day state, a tap on the icon edits the habit, a long press opens its details
  - Weekly: a card per habit with its active days and its marks for the current week; a tap on the card opens today's day state, a tap on the icon edits the habit, a long press on the card opens its details
  - Overall: habits (columns) by days (rows): the last 66 days plus the upcoming 5
  - Day progress per habit: `·` started, `\` 25%, `|` 50%, `/` 75%, `X` 100%, `▣` over 100%
  - Tap a date to mark the day as skipped ("—") for all not marked habits
  - Day Streak: the number of consecutive marked days ("—" days don't break it)
- Metrics
  - Daily completion (%) over the last 66 days
  - Habit details: description, target, completion of every day for the last year (like GitHub contributions), max and current streak
  - Goals of the habit: the (+) button adds a goal (title, description, and a new or existing group); goals are grouped by name, a tap marks a goal as finished, fully finished groups go last, and ↑ moves a group up; a long press on a goal edits it, and a long press on a group deletes it with all its goals (after confirmation)
- Schedule (Calendar)
  - Schedule a habit for 30 minutes, 1 hour, 2 hours, or a custom time range, repeated on the selected days of the week,
    with an optional note
  - Reminders (notifications)
  - Add to an external calendar (Apple, Google, Microsoft)
- Timer (from the Schedule)
  - Clock of the next 12 hours with the scheduled events, countdown of the current event, and the next events
  - Keeps the screen on, vibrates when an event starts, and asks for the habit’s completion when it ends
  - Pomodoro: 15 min, 30 min, 1 hour, 2 hours, or custom, optionally for a habit
- Settings
  - Import / Export of data
- User Experience
  - Automation: Siri Shortcuts (complete, add to, or skip a habit; habits left today) and Android intents for Tasker, Automate, or `adb`; the shown screen updates at once
  - Accessibility (WCAG 2.1): every action is named for screen readers, readable contrast in both themes, text scales with the system font size
