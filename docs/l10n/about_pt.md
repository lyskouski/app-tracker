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
Português Brasileiro (PT-BR) |
[Limba română (RO)](./about_ro.md) |
[Русский (RU)](./about_ru.md) |
[Türkçe (TR)](./about_tr.md) |
[Українська (UK-UA)](./about_uk.md) |
[Oʻzbekcha (UZ)](./about_uz.md) |
[简体中文 (ZH-CN)](./about_zh.md) |
[繁體中文 (ZH-TW)](./about_zh_TW.md) |

---

Lema: Pequenos passos. Progresso visível. Hábitos duradouros.

Palavras-chave: pomodoro,lembrete,planejador,calendário,foco,disciplina,autocuidado,tarefas,checklist,progresso

**Zwyka** -- aplicativo multiplataforma de código aberto para acompanhamento de hábitos, sem anúncios e sem limitações.
O objetivo da solução é criar um aplicativo de acompanhamento de hábitos intuitivo, eficiente e inclusivo.
Ele permite que os usuários gerenciem seus hábitos sem esforço, garantindo que ninguém fique para trás.

### Funcionalidades
- Hábitos
  - Título, observações, ícone (mais de 2.000 ícones Material) e cor (da paleta ou RGB / HEX personalizado)
  - Dias ativos da semana; os dias inativos são marcados com "—"
  - Arquivar (desativar) um hábito: ele fica acinzentado, é exibido por último e fica oculto na Agenda
  - Meta numérica, ex. 10000 passos: o dia guarda o valor alcançado, conclusão = valor / meta
  - Hábito a evitar (definido na criação): o ícone aparece riscado, um dia sem marca = 100%, uma marca dá 100% menos a sua porcentagem, a sequência conta os dias sem marcas
- Calendário de hábitos (página principal)
  - Hoje: os hábitos de hoje em lista, os concluídos e ignorados ficam abaixo; deslizar para a direita marca um hábito como feito (uma meta numérica pede o valor), deslizar para a esquerda o pula, um toque abre o estado do dia, um toque no ícone edita o hábito, um toque longo abre seus detalhes
  - Semanal: um cartão por hábito com seus dias ativos e marcas da semana atual; um toque no cartão abre o estado do dia de hoje, um toque no ícone edita o hábito, um toque longo no cartão abre seus detalhes
  - Geral: Hábitos (colunas) por dias (linhas): os últimos 66 dias mais os 5 próximos
  - Progresso do dia por hábito: `·` iniciado, `\` 25%, `|` 50%, `/` 75%, `X` 100%, `▣` acima de 100%
  - Toque em uma data para marcar o dia como pulado ("—") para todos os hábitos não marcados
  - Sequência de dias: o número de dias marcados consecutivos (os dias "—" não a interrompem)
- Métricas
  - Conclusão diária (%) nos últimos 66 dias
  - Detalhes do hábito: descrição, meta, conclusão de cada dia no último ano (como as contribuições do GitHub), sequência máxima e atual
  - Metas do hábito: o botão (+) adiciona uma meta (título, descrição e um grupo novo ou existente); as metas são agrupadas por nome, um toque marca a meta como concluída, grupos totalmente concluídos vão para o final e ↑ move um grupo para cima; um toque longo em uma meta a edita, e um toque longo em um grupo o exclui com todas as suas metas (após confirmação)
- Agenda (Calendário)
  - Agende um hábito para 30 minutos, 1 hora, 2 horas ou um intervalo de tempo personalizado, repetido nos dias da
    semana selecionados, com uma nota opcional
  - Lembretes (notificações)
  - Adicionar a um calendário externo (Apple, Google, Microsoft)
- Temporizador (a partir da Agenda)
  - Relógio das próximas 12 horas com os eventos agendados, contagem regressiva do evento atual e próximos eventos
  - Mantém a tela ligada, vibra quando um evento começa e pede a conclusão do hábito quando termina
  - Pomodoro: 15 min, 30 min, 1 hora, 2 horas ou personalizado, opcionalmente para um hábito
- Configurações
  - Criação de backup e restauração a partir de um backup (substitui os dados existentes)
  - Importação de outros apps: Loop Habit Tracker, HabitKit, Habitify, HabitBull
- Experiência do usuário
  - Automação: Atalhos da Siri (concluir, somar a um hábito ou pulá-lo; hábitos que faltam hoje) e intents do Android para Tasker, Automate ou `adb`; a tela exibida é atualizada na hora
  - Acessibilidade (WCAG 2.1): todas as ações têm nome para leitores de tela, contraste legível nos dois temas, o texto acompanha o tamanho da fonte do sistema
