[اللغة العربية (AR)](./about_ar.md) |
[Azərbaycan dili (AZ)](./about_az.md) |
[Беларуская (BE)](./about_be.md) |
[Čeština (CS)](./about_cs.md) |
Deutsch (DE) |
[English (EN-US)](./about_en.md) |
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

**Zwyka** -- quelloffene, plattformübergreifende Anwendung zum Verfolgen von Gewohnheiten ohne Werbung und
Einschränkungen. Ziel der Lösung ist es, eine Gewohnheits-Tracker-App zu schaffen, die intuitiv, effizient und inklusiv
ist. Sie ermöglicht es Nutzern, ihre Gewohnheiten mühelos zu verwalten, und sorgt dafür, dass niemand zurückgelassen wird.

### Funktionen
- Gewohnheiten
  - Titel, Anmerkungen, Symbol (über 2.000 Material-Symbole) und Farbe (Palette oder eigene RGB / HEX)
  - Aktive Wochentage; inaktive Tage werden mit „—“ markiert
  - Eine Gewohnheit archivieren (deaktivieren): Sie wird ausgegraut, zuletzt angezeigt und im Zeitplan ausgeblendet
  - Numerisches Ziel, z. B. 10000 Schritte: ein Tag speichert den erreichten Wert, Erfüllung = Wert / Ziel
- Gewohnheitskalender (Hauptseite)
  - Drei Ansichten, umschaltbar über die Tabs oben (die gewählte wird gemerkt): Heute, Woche und Gesamt
  - Heute: die heutigen Gewohnheiten als Liste, erledigte und übersprungene stehen darunter; Wischen nach rechts markiert eine Gewohnheit als erledigt (bei einem numerischen Ziel wird der Wert abgefragt), Wischen nach links überspringt sie, Tippen öffnet den Tagesstatus, langes Drücken bearbeitet die Gewohnheit
  - Woche: eine Karte pro Gewohnheit mit ihren aktiven Tagen und Markierungen der aktuellen Woche; langes Drücken auf die Karte bearbeitet die Gewohnheit
  - Gesamt: Gewohnheiten (Spalten) nach Tagen (Zeilen): die letzten 66 Tage plus die kommenden 5
  - Tagesfortschritt pro Gewohnheit: `·` begonnen, `\` 25 %, `|` 50 %, `/` 75 %, `X` 100 %, `▣` über 100 %
  - Eine Zelle kann auch als übersprungen ("—") markiert werden; langes Drücken auf eine Zelle löscht ihren Status
  - Langes Drücken auf eine Gewohnheit öffnet ihre Details
  - Fortschritt als Symbole oder als farbige Kreise dargestellt
  - Auf ein Datum tippen, um den Tag für alle nicht markierten Gewohnheiten als übersprungen („—“) zu markieren
  - Tagesserie: die Anzahl aufeinanderfolgender markierter Tage („—“-Tage unterbrechen sie nicht)
  - Heutiger Fortschritt: Gesamterfüllung der heutigen Gewohnheiten
- Kennzahlen
  - Tägliche Erfüllung (%) über die letzten 66 Tage
  - Gewohnheitsdetails: Beschreibung, Ziel, Erfüllung jedes Tages im letzten Jahr (wie GitHub-Beiträge), längste und aktuelle Serie
  - Ziele der Gewohnheit: Die Schaltfläche (+) fügt ein Ziel hinzu (Titel, Beschreibung und eine neue oder vorhandene Gruppe); Ziele werden nach Namen gruppiert, ein Tippen markiert ein Ziel als erledigt, vollständig erledigte Gruppen rücken ans Ende, ↑ verschiebt eine Gruppe nach oben; langes Drücken auf ein Ziel bearbeitet es, langes Drücken auf eine Gruppe löscht sie samt allen Zielen (nach Bestätigung)
- Zeitplan (Kalender)
  - Tagesansicht mit Stunden, wie in Google oder Outlook Kalender
  - Eine Gewohnheit für 30 Minuten, 1 Stunde, 2 Stunden oder einen eigenen Zeitraum planen, wiederholt an den gewählten
    Wochentagen, mit optionaler Notiz
  - Sich überschneidende Zeiträume werden nebeneinander angezeigt
  - Erinnerungen (Benachrichtigungen)
  - Zu einem externen Kalender hinzufügen (Apple, Google, Microsoft)
  - Die Schaltfläche (+) plant eine Gewohnheit
- Timer (aus dem Zeitplan)
  - Uhr der nächsten 12 Stunden mit den geplanten Terminen, Countdown des aktuellen Termins und die nächsten Termine
  - Hält den Bildschirm an, vibriert bei Beginn eines Termins und fragt am Ende nach der Erfüllung der Gewohnheit
  - Pomodoro: 15 Min., 30 Min., 1 Stunde, 2 Stunden oder eigene Dauer, optional für eine Gewohnheit
- Einstellungen
  - Lokalisierung
  - Design (System, hell, dunkel)
  - Wochenbeginn, Datumsformat
  - Reihenfolge der Gewohnheiten (nach Erstellungsdatum, letzter Änderung oder Titel; aufsteigend oder absteigend)
  - Import / Export von Daten
- Benutzererlebnis
  - Responsives & adaptives Design
  - Scrollen per Touch oder mit der Maus: `Scroll Wheel` für Tage, `Shift` + `Scroll Wheel` für Gewohnheiten
  - Tastatur: Pfeiltasten, `Page Up` / `Page Down`, `Home` / `End` scrollen die Tabelle; `Menu` / `Shift` + `F10` ersetzt das lange Drücken
  - Barrierefreiheit (WCAG 2.1): Bezeichnungen aller Aktionen für Screenreader, gut lesbarer Kontrast in beiden Themes, Text skaliert mit der Systemschriftgröße
