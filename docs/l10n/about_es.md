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

Lema: Pequeños pasos. Progreso visible. Hábitos duraderos.

Palabras clave: pomodoro,recordatorio,planificador,calendario,enfoque,disciplina,autocuidado,tareas,progreso

**Zwyka** -- aplicación multiplataforma de código abierto para el seguimiento de hábitos, sin anuncios ni limitaciones.
El objetivo de la solución es crear una aplicación de seguimiento de hábitos intuitiva, eficiente e inclusiva.
Que permita a los usuarios gestionar sus hábitos sin esfuerzo, garantizando que nadie se quede atrás.

### Funcionalidad
- Hábitos
  - Título, observaciones, icono (más de 2000 iconos Material) y color (paleta o RGB / HEX personalizado)
  - Días activos de la semana; los días inactivos se marcan con "—"
  - Archivar (desactivar) un hábito: se muestra en gris, al final y se oculta en el Horario
  - Objetivo numérico, p. ej. 10000 pasos: el día guarda el valor alcanzado, cumplimiento = valor / objetivo
  - Hábito a evitar (se elige al crearlo): el icono aparece tachado, un día sin marca = 100 %, una marca da 100 % menos su porcentaje, la racha cuenta los días sin marcas
- Calendario de hábitos (página principal)
  - Hoy: los hábitos de hoy como lista, los completados y omitidos van debajo; deslizar a la derecha marca un hábito como hecho (un objetivo numérico pide su valor), deslizar a la izquierda lo omite, un toque abre el estado del día, un toque en el icono edita el hábito y una pulsación larga abre sus detalles
  - Semanal: una tarjeta por hábito con sus días activos y sus marcas de la semana actual; un toque en la tarjeta abre el estado del día de hoy, un toque en el icono edita el hábito y una pulsación larga en la tarjeta abre sus detalles
  - General: Hábitos (columnas) por días (filas): los últimos 66 días más los 5 siguientes
  - Progreso diario por hábito: `·` iniciado, `\` 25 %, `|` 50 %, `/` 75 %, `X` 100 %, `▣` más del 100 %
  - Toca una fecha para marcar el día como omitido ("—") para todos los hábitos no marcados
  - Racha de días: el número de días marcados consecutivos (los días "—" no la interrumpen)
- Métricas
  - Cumplimiento diario (%) durante los últimos 66 días
  - Detalles del hábito: descripción, objetivo, cumplimiento de cada día del último año (como las contribuciones de GitHub), racha máxima y actual
  - Objetivos del hábito: el botón (+) añade un objetivo (título, descripción y un grupo nuevo o existente); los objetivos se agrupan por nombre, un toque marca un objetivo como cumplido, los grupos cumplidos por completo van al final y ↑ sube un grupo; una pulsación larga en un objetivo lo edita y una pulsación larga en un grupo lo elimina con todos sus objetivos (tras confirmar)
- Horario (Calendario)
  - Programa un hábito para 30 minutos, 1 hora, 2 horas o un intervalo personalizado, repetido los días de la semana
    seleccionados, con una nota opcional
  - Recordatorios (notificaciones)
  - Añadir a un calendario externo (Apple, Google, Microsoft)
- Temporizador (desde el Horario)
  - Reloj de las próximas 12 horas con los eventos programados, cuenta atrás del evento actual y los próximos eventos
  - Mantiene la pantalla encendida, vibra al empezar un evento y pide el cumplimiento del hábito al terminar
  - Pomodoro: 15 min, 30 min, 1 hora, 2 horas o personalizado, opcionalmente para un hábito
- Ajustes
  - Creación de copias de seguridad y restauración desde una copia (reemplaza los datos existentes)
- Experiencia de usuario
  - Automatización: Atajos de Siri (completar, sumar a un hábito u omitirlo; hábitos pendientes de hoy) e intents de Android para Tasker, Automate o `adb`; la pantalla mostrada se actualiza al instante
  - Accesibilidad (WCAG 2.1): todas las acciones tienen nombre para los lectores de pantalla, contraste legible en ambos temas, el texto se adapta al tamaño de fuente del sistema
