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
[Polski (PL)](./about_pl.md) |
[Português Brasileiro (PT-BR)](./about_pt.md) |
Limba română (RO) |
[Русский (RU)](./about_ru.md) |
[Türkçe (TR)](./about_tr.md) |
[Українська (UK-UA)](./about_uk.md) |
[Oʻzbekcha (UZ)](./about_uz.md) |
[简体中文 (ZH-CN)](./about_zh.md) |
[繁體中文 (ZH-TW)](./about_zh_TW.md) |

---

Moto: Pași mici. Progres vizibil. Obiceiuri durabile.

Cuvinte cheie: pomodoro,memento,planificator,calendar,concentrare,disciplină,îngrijire,sarcini,progres,motivație

**Zwyka** -- aplicație open-source, multiplatformă, pentru urmărirea obiceiurilor, fără reclame și fără limitări.
Scopul soluției este de a crea o aplicație de urmărire a obiceiurilor intuitivă, eficientă și incluzivă.
Aceasta le permite utilizatorilor să își gestioneze obiceiurile fără efort, asigurându-se că nimeni nu rămâne în urmă.

### Funcționalități
- Obiceiuri
  - Titlu, observații, pictogramă (peste 2.000 de pictograme Material) și culoare (din paletă sau RGB / HEX personalizat)
  - Zilele active ale săptămânii; zilele inactive sunt marcate cu "—"
  - Arhivarea (dezactivarea) unui obicei: acesta devine gri, este afișat ultimul și este ascuns în Program
  - Obiectiv numeric, de ex. 10000 de pași: ziua păstrează valoarea atinsă, îndeplinire = valoare / obiectiv
  - Obicei de evitat (ales la creare): pictograma este tăiată, o zi fără marcaj = 100%, un marcaj dă 100% minus procentul său, seria numără zilele fără marcaje
- Calendarul obiceiurilor (pagina principală)
  - Astăzi: obiceiurile de azi ca listă, cele finalizate și omise sunt dedesubt; glisarea la dreapta marchează un obicei ca făcut (o țintă numerică cere valoarea), glisarea la stânga îl omite, o atingere deschide starea zilei, o atingere pe pictogramă editează obiceiul, o apăsare lungă îi deschide detaliile
  - Săptămână: un card pentru fiecare obicei cu zilele active și marcajele din săptămâna curentă; o atingere pe card deschide starea zilei de azi, o atingere pe pictogramă editează obiceiul, o apăsare lungă pe card îi deschide detaliile
  - General: Obiceiuri (coloane) pe zile (rânduri): ultimele 66 de zile plus următoarele 5
  - Progresul zilnic pentru fiecare obicei: `·` început, `\` 25%, `|` 50%, `/` 75%, `X` 100%, `▣` peste 100%
  - Atingeți o dată pentru a marca ziua ca sărită ("—") pentru toate obiceiurile nemarcate
  - Seria de zile: numărul de zile marcate consecutiv (zilele "—" nu o întrerup)
- Statistici
  - Îndeplinirea zilnică (%) în ultimele 66 de zile
  - Detaliile obiceiului: descriere, obiectiv, îndeplinirea fiecărei zile din ultimul an (ca contribuțiile GitHub), seria maximă și cea curentă
  - Obiectivele obiceiului: butonul (+) adaugă un obiectiv (titlu, descriere și un grup nou sau existent); obiectivele sunt grupate după nume, o atingere marchează obiectivul ca îndeplinit, grupurile îndeplinite complet trec la final, iar ↑ mută un grup în sus; o apăsare lungă pe un obiectiv îl editează, iar o apăsare lungă pe un grup îl șterge împreună cu toate obiectivele sale (după confirmare)
- Program (Calendar)
  - Programați un obicei pentru 30 de minute, 1 oră, 2 ore sau un interval de timp personalizat, repetat în zilele
    selectate ale săptămânii, cu o notă opțională
  - Mementouri (notificări)
  - Adăugare într-un calendar extern (Apple, Google, Microsoft)
- Cronometru (din Program)
  - Ceas al următoarelor 12 ore cu evenimentele programate, numărătoare inversă a evenimentului curent și evenimentele următoare
  - Menține ecranul aprins, vibrează la începutul unui eveniment și cere îndeplinirea obiceiului la final
  - Pomodoro: 15 min, 30 min, 1 oră, 2 ore sau durată personalizată, opțional pentru un obicei
- Setări
  - Crearea unei copii de rezervă și restaurarea din ea (înlocuiește datele existente)
- Experiența utilizatorului
  - Automatizare: Comenzi rapide Siri (finalizarea, adăugarea la un obicei sau sărirea lui; obiceiurile rămase azi) și intenturi Android pentru Tasker, Automate sau `adb`; ecranul afișat se actualizează imediat
  - Accesibilitate (WCAG 2.1): fiecare acțiune are nume pentru cititoarele de ecran, contrast lizibil în ambele teme, textul urmează dimensiunea fontului de sistem
