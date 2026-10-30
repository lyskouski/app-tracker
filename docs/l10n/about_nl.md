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
Nederlands (NL) |
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

**Zwyka** -- open-source, cross-platform app om gewoontes bij te houden, zonder advertenties en beperkingen.
Het doel van de oplossing is een app voor het bijhouden van gewoontes te maken die intuïtief, efficiënt en inclusief is.
Zo kunnen gebruikers moeiteloos hun gewoontes beheren, terwijl niemand achterblijft.

### Functionaliteit
- Gewoontes
  - Titel, opmerkingen, pictogram (meer dan 2.000 Material-pictogrammen) en kleur (palet of eigen RGB / HEX)
  - Actieve dagen van de week; inactieve dagen worden gemarkeerd met "—"
  - Een gewoonte archiveren (deactiveren): deze wordt grijs weergegeven, als laatste getoond en verborgen in de Planning
  - Numeriek doel, bijv. 10000 stappen: een dag bewaart de behaalde waarde, voltooiing = waarde / doel
- Gewoontekalender (hoofdpagina)
  - Drie weergaven, te wisselen met de tabbladen bovenaan (de gekozen weergave wordt onthouden): Vandaag, Week en Totaal
  - Vandaag: de gewoontes van vandaag als lijst, voltooide en overgeslagen staan eronder; naar rechts vegen markeert een gewoonte als klaar (een numeriek doel vraagt om de waarde), naar links vegen slaat haar over, tikken opent de dagstatus, tikken op het pictogram bewerkt de gewoonte, lang drukken opent haar details
  - Week: een kaart per gewoonte met haar actieve dagen en markeringen van de huidige week; tikken op de kaart opent de dagstatus van vandaag, tikken op het pictogram bewerkt de gewoonte, lang drukken op de kaart opent haar details
  - Totaal: Gewoontes (kolommen) per dag (rijen): de afgelopen 66 dagen plus de komende 5
  - Dagvoortgang per gewoonte: `·` begonnen, `\` 25%, `|` 50%, `/` 75%, `X` 100%, `▣` meer dan 100%
  - Een cel kan ook als overgeslagen ("—") worden gemarkeerd; lang drukken op een cel verwijdert de status
  - Lang drukken op een gewoonte opent de details
  - Voortgang weergegeven als symbolen of als gekleurde cirkels
  - Tik op een datum om de dag als overgeslagen ("—") te markeren voor alle niet-gemarkeerde gewoontes
  - Dagenreeks: het aantal opeenvolgende gemarkeerde dagen ("—"-dagen onderbreken de reeks niet)
  - Voortgang van vandaag: totale voltooiing van de gewoontes van vandaag
- Statistieken
  - Dagelijkse voltooiing (%) over de afgelopen 66 dagen
  - Details van een gewoonte: beschrijving, doel, voltooiing van elke dag in het afgelopen jaar (zoals GitHub-bijdragen), langste en huidige reeks
  - Doelen van de gewoonte: de knop (+) voegt een doel toe (titel, beschrijving en een nieuwe of bestaande groep); doelen worden op naam gegroepeerd, een tik markeert een doel als behaald, volledig behaalde groepen gaan naar onderen en ↑ verplaatst een groep omhoog; lang drukken op een doel bewerkt het, en lang drukken op een groep verwijdert die met alle doelen (na bevestiging)
- Planning (Kalender)
  - Dagweergave met uren, zoals in Google of Outlook Agenda
  - Plan een gewoonte voor 30 minuten, 1 uur, 2 uur of een eigen tijdsbereik, herhaald op de gekozen dagen van de week,
    met een optionele notitie
  - Overlappende tijdsbereiken worden naast elkaar weergegeven
  - Herinneringen (meldingen)
  - Toevoegen aan een externe agenda (Apple, Google, Microsoft)
  - De knop (+) plant een gewoonte
- Timer (vanuit de Planning)
  - Klok van de komende 12 uur met de geplande afspraken, aftellen van de huidige afspraak en de volgende afspraken
  - Houdt het scherm aan, trilt bij de start van een afspraak en vraagt aan het einde naar de voltooiing van de gewoonte
  - Pomodoro: 15 min, 30 min, 1 uur, 2 uur of eigen duur, optioneel voor een gewoonte
- Instellingen
  - Lokalisatie
  - Thema (systeem, licht, donker)
  - Begin van de week, datumnotatie
  - Volgorde van gewoontes (op aanmaakdatum, laatste wijziging of titel; oplopend of aflopend)
  - Importeren / Exporteren van gegevens
- Gebruikerservaring
  - Responsief en adaptief ontwerp
  - Scrollen via aanraking of met de muis: `Scroll Wheel` voor dagen, `Shift` + `Scroll Wheel` voor gewoontes
  - Toetsenbord: pijltjes, `Page Up` / `Page Down`, `Home` / `End` scrollen door de tabel; `Menu` / `Shift` + `F10` vervangt lang indrukken; `Tab` gaat alleen door de gewoonten en de huidige dag, de cel met focus krijgt een rand en wordt in beeld gescrold
  - Sneltoetsen: `Ctrl` + `N` voegt een gewoonte toe (een afspraak, een doel, een Pomodoro); `Alt` + `1` … `0` opent de status van vandaag van de 1e … 10e gewoonte; `Enter` bevestigt een venster (100% voor een dagstatus), `Delete` slaat een dagstatus over of verwijdert een item, `Esc` sluit een venster
  - Toegankelijkheid (WCAG 2.1): elke actie heeft een naam voor schermlezers, leesbaar contrast in beide thema's, tekst schaalt mee met de systeemlettergrootte
