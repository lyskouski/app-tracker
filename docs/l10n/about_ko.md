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
한국어 (KO) |
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

**Zwyka** -- 광고와 제한이 없는 오픈 소스 크로스 플랫폼 습관 추적 애플리케이션입니다.
이 솔루션의 목표는 직관적이고 효율적이며 포용적인 습관 추적 애플리케이션을 만드는 것입니다.
누구도 소외되지 않도록 하면서 사용자가 손쉽게 자신의 습관을 관리할 수 있도록 돕습니다.

<img title="Zwyka: Main Screen" src="../design-flow/screenshots/main_1.jpg" width="240" /> <img title="Zwyka: Calendar" src="../design-flow/screenshots/calendar.jpg" width="240" /> <img title="Zwyka: Metrics" src="../design-flow/screenshots/metrics.jpg" width="240" />

### 기능
- 습관
  - 제목, 메모, 아이콘(2,000개 이상의 Material 아이콘), 색상(팔레트 또는 사용자 지정 RGB / HEX)
  - 요일별 활성 설정; 비활성 요일은 "—"로 표시됩니다
  - 습관 보관(비활성화): 회색으로 표시되고, 맨 마지막에 나타나며, 일정에서 숨겨집니다
  - 숫자 목표(예: 10000 걸음): 하루에 달성한 값을 저장하며, 달성률 = 값 / 목표
- 습관 캘린더(메인 페이지)
  - 날짜(행)별 습관(열): 최근 66일과 앞으로의 5일
  - 습관별 하루 진행률: `·` 시작, `\` 25%, `|` 50%, `/` 75%, `X` 100%, `▣` 100% 초과
  - 셀을 건너뜀("—")으로 표시할 수도 있습니다. 셀을 길게 누르면 상태가 삭제됩니다
  - 습관을 길게 누르면 상세 정보가 열립니다
  - 진행률을 기호 또는 색상 원으로 표시
  - 날짜를 탭하면 표시되지 않은 모든 습관에 대해 해당 날짜를 건너뜀("—")으로 표시합니다
  - 연속 일수: 연속으로 표시된 날의 수("—"인 날은 연속 기록을 끊지 않습니다)
  - 오늘의 진행률: 오늘 습관의 전체 달성률
- 통계
  - 최근 66일 동안의 일일 달성률(%)
  - 습관 상세: 설명, 목표, 최근 1년간 하루별 달성률(GitHub 기여 그래프처럼), 최장 및 현재 연속
- 일정(캘린더)
  - Google 캘린더나 Outlook 캘린더처럼 시간대가 표시되는 일간 보기
  - 습관을 30분, 1시간, 2시간 또는 사용자 지정 시간 범위로 일정에 추가하고, 선택한 요일마다 반복하며,
    선택적으로 메모를 남길 수 있습니다
  - 겹치는 시간 범위는 나란히 표시됩니다
  - 리마인더(알림)
  - 외부 캘린더(Apple, Google, Microsoft)에 추가
  - (+) 버튼으로 습관을 일정에 추가
- 타이머(일정에서)
  - 예정된 일정이 표시된 앞으로 12시간의 시계, 현재 일정의 카운트다운, 다음 일정
  - 화면을 켜 둔 채로 유지하고, 일정이 시작되면 진동하며, 끝나면 습관 달성률을 묻습니다
  - 뽀모도로: 15분, 30분, 1시간, 2시간 또는 사용자 지정, 습관 지정은 선택
- 설정
  - 현지화
  - 테마(시스템, 라이트, 다크)
  - 주 시작 요일, 날짜 형식
  - 습관 정렬 순서(생성일, 최근 수정일 또는 제목 기준; 오름차순 또는 내림차순)
  - 데이터 가져오기 / 내보내기
- 사용자 경험
  - 반응형 및 적응형 디자인
  - 터치 또는 마우스로 스크롤: 날짜는 `Scroll Wheel`, 습관은 `Shift` + `Scroll Wheel`
