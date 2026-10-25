[اللغة العربية (AR)](./about_ar.md) |
[Azərbaycan dili (AZ)](./about_az.md) |
[Беларуская (BE)](./about_be.md) |
[Čeština (CS)](./about_cs.md) |
[Deutsch (DE)](./about_de.md) |
[English (EN-US)](./about_en.md) |
Español (ES) |
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

**Zwyka** -- aplicación multiplataforma de código abierto para el seguimiento de hábitos, sin anuncios ni limitaciones.
El objetivo de la solución es crear una aplicación de seguimiento de hábitos intuitiva, eficiente e inclusiva.
Que permita a los usuarios gestionar sus hábitos sin esfuerzo, garantizando que nadie se quede atrás.

### Funcionalidad
- Hábitos
  - Título, observaciones, icono (más de 2000 iconos Material) y color (paleta o RGB / HEX personalizado)
  - Días activos de la semana; los días inactivos se marcan con "—"
  - Archivar (desactivar) un hábito: se muestra en gris, al final y se oculta en el Horario
  - Objetivo numérico, p. ej. 10000 pasos: el día guarda el valor alcanzado, cumplimiento = valor / objetivo
- Calendario de hábitos (página principal)
  - Hábitos (columnas) por días (filas): los últimos 66 días más los 5 siguientes
  - Progreso diario por hábito: `·` iniciado, `\` 25 %, `|` 50 %, `/` 75 %, `X` 100 %, `▣` más del 100 %
  - Una celda también puede marcarse como omitida ("—"); una pulsación larga en una celda elimina su estado
  - Una pulsación larga en un hábito abre sus detalles
  - Progreso mostrado como símbolos o como círculos de color
  - Toca una fecha para marcar el día como omitido ("—") para todos los hábitos no marcados
  - Racha de días: el número de días marcados consecutivos (los días "—" no la interrumpen)
  - Progreso de hoy: cumplimiento total de los hábitos de hoy
- Métricas
  - Cumplimiento diario (%) durante los últimos 66 días
  - Detalles del hábito: descripción, objetivo, cumplimiento de cada día del último año (como las contribuciones de GitHub), racha máxima y actual
- Horario (Calendario)
  - Vista diaria por horas, como en Google o Outlook Calendar
  - Programa un hábito para 30 minutos, 1 hora, 2 horas o un intervalo personalizado, repetido los días de la semana
    seleccionados, con una nota opcional
  - Los intervalos de tiempo superpuestos se muestran uno al lado del otro
  - Recordatorios (notificaciones)
  - Añadir a un calendario externo (Apple, Google, Microsoft)
  - El botón (+) programa un hábito
- Temporizador (desde el Horario)
  - Reloj de las próximas 12 horas con los eventos programados, cuenta atrás del evento actual y los próximos eventos
  - Mantiene la pantalla encendida, vibra al empezar un evento y pide el cumplimiento del hábito al terminar
  - Pomodoro: 15 min, 30 min, 1 hora, 2 horas o personalizado, opcionalmente para un hábito
- Ajustes
  - Localización
  - Tema (sistema, claro, oscuro)
  - Inicio de la semana, formato de fecha
  - Orden de los hábitos (por fecha de creación, última actualización o título; ascendente o descendente)
  - Importación / Exportación de datos
- Experiencia de usuario
  - Diseño responsivo y adaptativo
  - Desplazamiento táctil o con el ratón: `Scroll Wheel` para los días, `Shift` + `Scroll Wheel` para los hábitos
