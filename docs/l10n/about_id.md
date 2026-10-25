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
Bahasa Indonesia (ID) |
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

**Zwyka** -- aplikasi pelacak kebiasaan sumber terbuka dan lintas platform tanpa iklan dan tanpa batasan.
Tujuan solusi ini adalah menciptakan aplikasi pelacak kebiasaan yang intuitif, efisien, dan inklusif,
yang memungkinkan pengguna mengelola kebiasaan mereka dengan mudah sambil memastikan tidak ada seorang pun yang
tertinggal.

<img title="Zwyka: Main Screen" src="../design-flow/screenshots/main_1.jpg" width="240" /> <img title="Zwyka: Calendar" src="../design-flow/screenshots/calendar.jpg" width="240" /> <img title="Zwyka: Metrics" src="../design-flow/screenshots/metrics.jpg" width="240" />

### Fungsionalitas
- Kebiasaan
  - Judul, catatan, ikon (lebih dari 2.000 ikon Material), dan warna (palet atau RGB / HEX khusus)
  - Hari aktif dalam seminggu; hari tidak aktif ditandai dengan "—"
  - Arsipkan (nonaktifkan) kebiasaan: kebiasaan ditampilkan berwarna abu-abu, diletakkan paling akhir, dan disembunyikan
    di Jadwal
  - Target angka, mis. 10000 langkah: hari menyimpan nilai yang dicapai, penyelesaian = nilai / target
- Kalender Kebiasaan (halaman utama)
  - Kebiasaan (kolom) per hari (baris): 66 hari terakhir ditambah 5 hari mendatang
  - Kemajuan harian per kebiasaan: `·` dimulai, `\` 25%, `|` 50%, `/` 75%, `X` 100%, `▣` lebih dari 100%
  - Sel juga dapat ditandai sebagai dilewati ("—"); tekan lama pada sel untuk menghapus statusnya
  - Tekan lama pada kebiasaan untuk membuka detailnya
  - Kemajuan ditampilkan sebagai simbol atau sebagai lingkaran berwarna
  - Ketuk tanggal untuk menandai hari tersebut sebagai dilewati ("—") untuk semua kebiasaan yang belum ditandai
  - Rentetan Hari: jumlah hari berturut-turut yang ditandai (hari "—" tidak memutusnya)
  - Kemajuan hari ini: penyelesaian keseluruhan kebiasaan hari ini
- Statistik
  - Penyelesaian harian (%) selama 66 hari terakhir
  - Detail kebiasaan: deskripsi, target, penyelesaian setiap hari selama setahun terakhir (seperti kontribusi GitHub), rentetan terpanjang dan saat ini
- Jadwal (Kalender)
  - Tampilan harian dengan jam, seperti Google atau Outlook Calendar
  - Jadwalkan kebiasaan selama 30 menit, 1 jam, 2 jam, atau rentang waktu khusus, diulang pada hari-hari yang dipilih
    dalam seminggu, dengan catatan opsional
  - Rentang waktu yang tumpang tindih ditampilkan berdampingan
  - Pengingat (notifikasi)
  - Tambahkan ke kalender eksternal (Apple, Google, Microsoft)
- Pengaturan
  - Lokalisasi
  - Mode Tema (sistem, terang, gelap)
  - Awal minggu, format tanggal
  - Urutan kebiasaan (menurut tanggal dibuat, pembaruan terakhir, atau judul; naik atau turun)
  - Impor / Ekspor data
- Pengalaman Pengguna
  - Desain Responsif & Adaptif
  - Gulir dengan sentuhan, atau dengan mouse: `Scroll Wheel` untuk hari, `Shift` + `Scroll Wheel` untuk kebiasaan
