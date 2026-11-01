[اللغة العربية (AR)](./about_ar.md) |
[Azərbaycan dili (AZ)](./about_az.md) |
[Беларуская (BE)](./about_be.md) |
[Čeština (CS)](./about_cs.md) |
[Deutsch (DE)](./about_de.md) |
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
Polski (PL) |
[Português Brasileiro (PT-BR)](./about_pt.md) |
[Limba română (RO)](./about_ro.md) |
[Русский (RU)](./about_ru.md) |
[Türkçe (TR)](./about_tr.md) |
[Українська (UK-UA)](./about_uk.md) |
[Oʻzbekcha (UZ)](./about_uz.md) |
[简体中文 (ZH-CN)](./about_zh.md) |
[繁體中文 (ZH-TW)](./about_zh_TW.md) |

---

Motto: Małe kroki. Widoczne postępy. Trwałe nawyki.

Słowa kluczowe: pomodoro,przypomnienie,planer,kalendarz,skupienie,dyscyplina,dbanie,zadania,lista,postęp,motywacja

**Zwyka** -- otwartoźródłowa, wieloplatformowa aplikacja do śledzenia nawyków bez reklam i ograniczeń.
Celem rozwiązania jest stworzenie aplikacji do śledzenia nawyków, która jest intuicyjna, wydajna i inkluzywna.
Pozwala ona użytkownikom bez wysiłku zarządzać swoimi nawykami, dbając o to, by nikt nie został pominięty.

### Funkcjonalność
- Nawyki
  - Nazwa, uwagi, ikona (ponad 2000 ikon Material) i kolor (z palety lub własny RGB / HEX)
  - Aktywne dni tygodnia; nieaktywne dni są oznaczone symbolem "—"
  - Archiwizacja (dezaktywacja) nawyku: jest on wyszarzony, wyświetlany na końcu i ukryty w Harmonogramie
  - Cel liczbowy, np. 10000 kroków: dzień przechowuje osiągniętą wartość, realizacja = wartość / cel
  - Nawyk do unikania (wybierany przy tworzeniu): ikona jest przekreślona, dzień bez znaku = 100%, znak daje 100% minus swój procent, seria liczy dni bez znaków
- Kalendarz nawyków (strona główna)
  - Dziś: dzisiejsze nawyki jako lista, wykonane i pominięte trafiają niżej; przesunięcie w prawo oznacza nawyk jako zrobiony (cel liczbowy pyta o wartość), przesunięcie w lewo go pomija, dotknięcie otwiera stan dnia, dotknięcie ikony edytuje nawyk, długie naciśnięcie otwiera jego szczegóły
  - Tydzień: karta dla każdego nawyku z jego aktywnymi dniami i oznaczeniami z bieżącego tygodnia; dotknięcie karty otwiera dzisiejszy stan dnia, dotknięcie ikony edytuje nawyk, długie naciśnięcie karty otwiera jego szczegóły
  - Ogółem: Nawyki (kolumny) według dni (wiersze): ostatnie 66 dni oraz 5 nadchodzących
  - Postęp dnia dla każdego nawyku: `·` rozpoczęty, `\` 25%, `|` 50%, `/` 75%, `X` 100%, `▣` ponad 100%
  - Stuknij datę, aby oznaczyć dzień jako pominięty ("—") dla wszystkich nieoznaczonych nawyków
  - Seria dni: liczba kolejnych oznaczonych dni (dni "—" jej nie przerywają)
- Statystyki
  - Dzienna realizacja (%) w ciągu ostatnich 66 dni
  - Szczegóły nawyku: opis, cel, realizacja każdego dnia z ostatniego roku (jak wkład na GitHub), najdłuższa i obecna seria
  - Cele nawyku: przycisk (+) dodaje cel (tytuł, opis oraz nowa lub istniejąca grupa); cele są grupowane według nazwy, dotknięcie oznacza cel jako osiągnięty, w pełni osiągnięte grupy trafiają na koniec, a ↑ przesuwa grupę wyżej; długie naciśnięcie celu pozwala go edytować, a długie naciśnięcie grupy usuwa ją wraz ze wszystkimi celami (po potwierdzeniu)
- Harmonogram (Kalendarz)
  - Zaplanuj nawyk na 30 minut, 1 godzinę, 2 godziny lub własny przedział czasu, powtarzany w wybrane dni tygodnia,
    z opcjonalną notatką
  - Przypomnienia (powiadomienia)
  - Dodawanie do zewnętrznego kalendarza (Apple, Google, Microsoft)
- Minutnik (z Harmonogramu)
  - Zegar najbliższych 12 godzin z zaplanowanymi wydarzeniami, odliczanie bieżącego wydarzenia i następne wydarzenia
  - Ekran nie gaśnie; wibracja na początku wydarzenia i pytanie o realizację nawyku po jego zakończeniu
  - Pomodoro: 15 min, 30 min, 1 godzina, 2 godziny lub własny czas, opcjonalnie dla nawyku
- Ustawienia
  - Import / Eksport danych
- Doświadczenie użytkownika
  - Automatyzacja: Skróty Siri (wykonanie, dodanie do nawyku lub jego pominięcie; nawyki pozostałe na dziś) oraz intencje Androida dla Tasker, Automate lub `adb`; wyświetlany ekran od razu się odświeża
  - Dostępność (WCAG 2.1): nazwy wszystkich akcji dla czytników ekranu, czytelny kontrast w obu motywach, tekst skaluje się z systemowym rozmiarem czcionki
