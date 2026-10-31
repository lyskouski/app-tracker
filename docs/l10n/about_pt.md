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

**Zwyka** -- aplicativo multiplataforma de código aberto para acompanhamento de hábitos, sem anúncios e sem limitações.
O objetivo da solução é criar um aplicativo de acompanhamento de hábitos intuitivo, eficiente e inclusivo.
Ele permite que os usuários gerenciem seus hábitos sem esforço, garantindo que ninguém fique para trás.

### Funcionalidades
- Hábitos
  - Título, observações, ícone (mais de 2.000 ícones Material) e cor (da paleta ou RGB / HEX personalizado)
  - Dias ativos da semana; os dias inativos são marcados com "—"
  - Arquivar (desativar) um hábito: ele fica acinzentado, é exibido por último e fica oculto na Agenda
  - Meta numérica, ex. 10000 passos: o dia guarda o valor alcançado, conclusão = valor / meta
- Calendário de hábitos (página principal)
  - Três visualizações, trocadas pelas abas no topo (a escolhida é lembrada): Hoje, Semanal e Geral
  - Hoje: os hábitos de hoje em lista, os concluídos e ignorados ficam abaixo; deslizar para a direita marca um hábito como feito (uma meta numérica pede o valor), deslizar para a esquerda o pula, um toque abre o estado do dia, um toque no ícone edita o hábito, um toque longo abre seus detalhes
  - Semanal: um cartão por hábito com seus dias ativos e marcas da semana atual; um toque no cartão abre o estado do dia de hoje, um toque no ícone edita o hábito, um toque longo no cartão abre seus detalhes
  - Geral: Hábitos (colunas) por dias (linhas): os últimos 66 dias mais os 5 próximos
  - Progresso do dia por hábito: `·` iniciado, `\` 25%, `|` 50%, `/` 75%, `X` 100%, `▣` acima de 100%
  - Uma célula também pode ser marcada como pulada ("—"); um toque longo em uma célula exclui seu estado
  - Um toque longo em um hábito abre seus detalhes
  - Progresso exibido como símbolos ou como círculos coloridos
  - Toque em uma data para marcar o dia como pulado ("—") para todos os hábitos não marcados
  - Sequência de dias: o número de dias marcados consecutivos (os dias "—" não a interrompem)
  - Progresso de hoje: conclusão geral dos hábitos de hoje
- Métricas
  - Conclusão diária (%) nos últimos 66 dias
  - Detalhes do hábito: descrição, meta, conclusão de cada dia no último ano (como as contribuições do GitHub), sequência máxima e atual
  - Metas do hábito: o botão (+) adiciona uma meta (título, descrição e um grupo novo ou existente); as metas são agrupadas por nome, um toque marca a meta como concluída, grupos totalmente concluídos vão para o final e ↑ move um grupo para cima; um toque longo em uma meta a edita, e um toque longo em um grupo o exclui com todas as suas metas (após confirmação)
- Agenda (Calendário)
  - Visualização do dia com horas, como no Google Agenda ou no Calendário do Outlook
  - Agende um hábito para 30 minutos, 1 hora, 2 horas ou um intervalo de tempo personalizado, repetido nos dias da
    semana selecionados, com uma nota opcional
  - Intervalos de tempo sobrepostos são exibidos lado a lado
  - Lembretes (notificações)
  - Adicionar a um calendário externo (Apple, Google, Microsoft)
  - O botão (+) agenda um hábito
- Temporizador (a partir da Agenda)
  - Relógio das próximas 12 horas com os eventos agendados, contagem regressiva do evento atual e próximos eventos
  - Mantém a tela ligada, vibra quando um evento começa e pede a conclusão do hábito quando termina
  - Pomodoro: 15 min, 30 min, 1 hora, 2 horas ou personalizado, opcionalmente para um hábito
- Configurações
  - Localização
  - Tema (sistema, claro, escuro)
  - Início da semana, formato de data
  - Ordem dos hábitos (por data de criação, última atualização ou título; crescente ou decrescente)
  - Importação / Exportação de dados
- Experiência do usuário
  - Design responsivo e adaptável
  - Rolagem por toque ou com o mouse: `Scroll Wheel` para os dias, `Shift` + `Scroll Wheel` para os hábitos
  - Teclado: as setas, `Page Up` / `Page Down`, `Home` / `End` rolam a tabela; `Menu` / `Shift` + `F10` / `Ctrl` + `Enter` substitui o toque longo; `Tab` percorre apenas os hábitos e o dia atual, a célula em foco recebe um contorno e é rolada para a área visível
  - Atalhos: `Ctrl` + `N` adiciona um hábito (um evento, uma meta, um Pomodoro); `Alt` + `1` … `0` abre o estado de hoje do 1º … 10º hábito; `Enter` confirma uma janela (100% para um estado do dia), `Delete` pula um estado do dia ou exclui um item, `Esc` fecha uma janela, `Backspace` volta
  - Acessibilidade (WCAG 2.1): todas as ações têm nome para leitores de tela, contraste legível nos dois temas, o texto acompanha o tamanho da fonte do sistema
