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

**Zwyka** -- open-source cross-platform habits tracking application without Ads and limitations.
The goal of the solution is to create a habits tracking application that is intuitive, efficient, and inclusive.
That empowers users to effortlessly manage their habits while ensuring that no one is left behind.

### Functionality
- Habits
  - Title, remarks, icon (2,000+ Material icons), and color (palette or custom RGB / HEX)
  - Active days of the week; inactive days are marked with "—"
  - Archive (deactivate) a habit: it is grayed out, shown last, and hidden in the Schedule
  - Numeric target, e.g. 10000 steps: a day keeps the reached value, completion is value / target
- Habits Calendar (main page)
  - Habits (columns) by days (rows): the last 66 days plus the upcoming 5
  - Day progress per habit: `·` started, `\` 25%, `|` 50%, `/` 75%, `X` 100%, `▣` over 100%
  - A cell can also be marked as skipped ("—"); long press on a cell deletes its state
  - Long press on a habit opens its details
  - Progress shown as symbols or as colored circles
  - Tap a date to mark the day as skipped ("—") for all not marked habits
  - Day Streak: the number of consecutive marked days ("—" days don't break it)
  - Today's progress: overall completion of today's habits
- Metrics
  - Daily completion (%) over the last 66 days
  - Habit details: description, target, completion of every day for the last year (like GitHub contributions), max and current streak
- Schedule (Calendar)
  - Day view with hours, like Google or Outlook Calendar
  - Schedule a habit for 30 minutes, 1 hour, 2 hours, or a custom time range, repeated on the selected days of the week,
    with an optional note
  - Overlapping time ranges are shown side by side
  - Reminders (notifications)
  - Add to an external calendar (Apple, Google, Microsoft)
  - The (+) button schedules a habit
- Timer (from the Schedule)
  - Clock of the next 12 hours with the scheduled events, countdown of the current event, and the next events
  - Keeps the screen on, vibrates when an event starts, and asks for the habit’s completion when it ends
  - Pomodoro: 15 min, 30 min, 1 hour, 2 hours, or custom, optionally for a habit
- Settings
  - Localization
  - Theme Mode (system, light, dark)
  - Start of the week, dates format
  - Habits order (by creation date, last update, or title; ascending or descending)
  - Import / Export of data
- User Experience
  - Responsive & Adaptive Design
  - Scroll by touch, or by mouse: `Scroll Wheel` for days, `Shift` + `Scroll Wheel` for habits
