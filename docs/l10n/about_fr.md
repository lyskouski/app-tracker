[اللغة العربية (AR)](./about_ar.md) |
[Azərbaycan dili (AZ)](./about_az.md) |
[Беларуская (BE)](./about_be.md) |
[Čeština (CS)](./about_cs.md) |
[Deutsch (DE)](./about_de.md) |
[English (EN-US)](./about_en.md) |
[Español (ES)](./about_es.md) |
[فارسی (FA)](./about_fa.md) |
Français (FR) |
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

Devise : Petits pas. Progrès visibles. Habitudes durables.

Mots-clés : pomodoro,rappel,planificateur,calendrier,concentration,discipline,bien-être,tâches,progrès

**Zwyka** -- application open source et multiplateforme de suivi des habitudes, sans publicités ni limitations.
L’objectif de la solution est de créer une application de suivi des habitudes intuitive, efficace et inclusive,
qui permette aux utilisateurs de gérer leurs habitudes sans effort tout en veillant à ce que personne ne soit laissé
de côté.

### Fonctionnalités
- Habitudes
  - Titre, remarques, icône (plus de 2 000 icônes Material) et couleur (palette ou RVB / HEX personnalisé)
  - Jours actifs de la semaine ; les jours inactifs sont marqués par « — »
  - Archiver (désactiver) une habitude : elle est grisée, affichée en dernier et masquée dans le Planning
  - Objectif numérique, p. ex. 10000 pas : le jour garde la valeur atteinte, réalisation = valeur / objectif
  - Habitude à éviter (choisie à la création) : l’icône est barrée, un jour sans marque = 100 %, une marque donne 100 % moins son pourcentage, la série compte les jours sans marque
- Calendrier des habitudes (page principale)
  - Aujourd’hui : les habitudes du jour en liste, les terminées et ignorées en dessous ; un balayage vers la droite marque une habitude comme faite (un objectif numérique demande sa valeur), vers la gauche la passe, un appui ouvre l’état du jour, un appui sur l’icône modifie l’habitude, un appui long ouvre ses détails
  - Semaine : une carte par habitude avec ses jours actifs et ses marques de la semaine en cours ; un appui sur la carte ouvre l’état du jour d’aujourd’hui, un appui sur l’icône modifie l’habitude, un appui long sur la carte ouvre ses détails
  - Global : Habitudes (colonnes) par jours (lignes) : les 66 derniers jours plus les 5 à venir
  - Progression du jour par habitude : `·` commencée, `\` 25 %, `|` 50 %, `/` 75 %, `X` 100 %, `▣` plus de 100 %
  - Touchez une date pour marquer le jour comme sauté (« — ») pour toutes les habitudes non marquées
  - Série de jours : le nombre de jours marqués consécutifs (les jours « — » ne l’interrompent pas)
- Statistiques
  - Réalisation quotidienne (%) sur les 66 derniers jours
  - Détails de l’habitude : description, objectif, réalisation de chaque jour sur la dernière année (comme les contributions GitHub), séries maximale et actuelle
  - Objectifs de l’habitude : le bouton (+) ajoute un objectif (titre, description et un groupe nouveau ou existant) ; les objectifs sont regroupés par nom, un appui marque un objectif comme atteint, les groupes entièrement atteints passent en dernier et ↑ fait monter un groupe ; un appui long sur un objectif permet de le modifier, et un appui long sur un groupe le supprime avec tous ses objectifs (après confirmation)
- Planning (Calendrier)
  - Planifier une habitude pour 30 minutes, 1 heure, 2 heures ou une plage horaire personnalisée, répétée les jours
    de la semaine sélectionnés, avec une note facultative
  - Rappels (notifications)
  - Ajout à un calendrier externe (Apple, Google, Microsoft)
- Minuteur (depuis le Planning)
  - Horloge des 12 prochaines heures avec les événements planifiés, compte à rebours de l’événement en cours et prochains événements
  - Garde l’écran allumé, vibre au début d’un événement et demande la réalisation de l’habitude à sa fin
  - Pomodoro : 15 min, 30 min, 1 heure, 2 heures ou durée personnalisée, éventuellement pour une habitude
- Paramètres
  - Importation / Exportation des données
- Expérience utilisateur
  - Automatisation : Raccourcis Siri (terminer, compléter ou sauter une habitude ; habitudes restantes du jour) et intents Android pour Tasker, Automate ou `adb` ; l’écran affiché est mis à jour aussitôt
  - Accessibilité (WCAG 2.1) : chaque action est nommée pour les lecteurs d'écran, contraste lisible dans les deux thèmes, le texte suit la taille de police du système
