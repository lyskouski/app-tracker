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
Português Europeu (PT-PT) |
[Limba română (RO)](./about_ro.md) |
[Русский (RU)](./about_ru.md) |
[Türkçe (TR)](./about_tr.md) |
[Українська (UK-UA)](./about_uk.md) |
[Oʻzbekcha (UZ)](./about_uz.md) |
[简体中文 (ZH-CN)](./about_zh.md) |
[繁體中文 (ZH-TW)](./about_zh_TW.md) |

---

Lema: Pequenos passos. Progresso visível. Hábitos duradouros.

Palavras-chave: pomodoro,lembrete,planeador,calendário,foco,disciplina,autocuidado,tarefas,checklist,progresso

**Zwyka** -- aplicação multiplataforma de código aberto para acompanhamento de hábitos, sem anúncios e sem limitações.
O objetivo da solução é criar uma aplicação de acompanhamento de hábitos intuitiva, eficiente e inclusiva.
Permite que os utilizadores giram os seus hábitos sem esforço, garantindo que ninguém fica para trás.

### Funcionalidades
- Hábitos
  - Título, observações, ícone (mais de 2000 ícones Material) e cor (da paleta ou RGB / HEX personalizado)
  - Dias ativos da semana; os dias inativos são marcados com "—"
  - Arquivar (desativar) um hábito: fica a cinzento, é apresentado em último lugar e fica oculto na Agenda
  - Objetivo numérico, p. ex. 10000 passos: o dia guarda o valor alcançado, conclusão = valor / objetivo
  - Hábito a evitar (definido na criação): o ícone aparece riscado, um dia sem marca = 100%, uma marca dá 100% menos a sua percentagem, a sequência conta os dias sem marcas
- Calendário de hábitos (página principal)
  - Hoje: os hábitos de hoje em lista, os concluídos e ignorados ficam abaixo; deslizar para a direita marca um hábito como feito (um objetivo numérico pede o valor), deslizar para a esquerda salta-o, um toque abre o estado do dia, um toque no ícone edita o hábito, um toque longo abre os seus detalhes
  - Semanal: um cartão por hábito com os seus dias ativos e marcas da semana atual; um toque no cartão abre o estado do dia de hoje, um toque no ícone edita o hábito, um toque longo no cartão abre os seus detalhes
  - Geral: Hábitos (colunas) por dias (linhas): os últimos 66 dias mais os 5 seguintes
  - Progresso do dia por hábito: `·` iniciado, `\` 25%, `|` 50%, `/` 75%, `X` 100%, `▣` acima de 100%
  - Toque numa data para marcar o dia como saltado ("—") para todos os hábitos não marcados
  - Sequência de dias: o número de dias marcados consecutivos (os dias "—" não a interrompem)
- Métricas
  - Conclusão diária (%) nos últimos 66 dias
  - Detalhes do hábito: descrição, objetivo, conclusão de cada dia no último ano (como as contribuições do GitHub), sequência máxima e atual
  - Objetivos do hábito: o botão (+) adiciona um objetivo (título, descrição e um grupo novo ou existente); os objetivos são agrupados por nome, um toque marca o objetivo como concluído, os grupos totalmente concluídos passam para o fim e ↑ move um grupo para cima; um toque longo num objetivo edita-o, e um toque longo num grupo elimina-o com todos os seus objetivos (após confirmação)
- Agenda (Calendário)
  - Agende um hábito para 30 minutos, 1 hora, 2 horas ou um intervalo de tempo personalizado, repetido nos dias da
    semana selecionados, com uma nota opcional
  - Lembretes (notificações)
  - Adicionar a um calendário externo (Apple, Google, Microsoft)
- Temporizador (a partir da Agenda)
  - Relógio das próximas 12 horas com os eventos agendados, contagem decrescente do evento atual e próximos eventos
  - Mantém o ecrã ligado, vibra quando um evento começa e pede a conclusão do hábito quando termina
  - Pomodoro: 15 min, 30 min, 1 hora, 2 horas ou personalizado, opcionalmente para um hábito
- Definições
  - Criação de cópia de segurança e restauro a partir de uma cópia de segurança (substitui os dados existentes)
  - Importação de outras aplicações: Loop Habit Tracker, HabitKit, Habitify, HabitBull
- Experiência do utilizador
  - Automação: Atalhos da Siri (concluir, somar a um hábito ou saltá-lo; hábitos que faltam hoje) e intents do Android para Tasker, Automate ou `adb`; o ecrã apresentado é atualizado de imediato
  - Acessibilidade (WCAG 2.1): todas as ações têm nome para leitores de ecrã, contraste legível nos dois temas, o texto acompanha o tamanho de letra do sistema
