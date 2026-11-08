package app.noor.prayer

import java.time.LocalDate
import java.time.chrono.HijrahDate
import java.time.temporal.ChronoField

/** Islamic (Umm al-Qura) calendar helpers with a user-adjustable day offset for moon-sighting differences. */
object Hijri {
    class Ymd(val y: Int, val m: Int, val d: Int)

    fun of(date: LocalDate): Ymd {
        val h = HijrahDate.from(date.plusDays(Prefs.hijriOffset.toLong()))
        return Ymd(h.get(ChronoField.YEAR), h.get(ChronoField.MONTH_OF_YEAR), h.get(ChronoField.DAY_OF_MONTH))
    }

    fun toGregorian(y: Int, m: Int, d: Int): LocalDate =
        LocalDate.from(HijrahDate.of(y, m, d)).minusDays(Prefs.hijriOffset.toLong())

    fun monthLength(y: Int, m: Int): Int = HijrahDate.of(y, m, 1).lengthOfMonth()

    fun format(date: LocalDate): String {
        val h = of(date)
        return nd("${h.d} ${monthName(h.m)} ${h.y}")
    }

    fun monthName(m: Int): String = HIJRI_MONTHS[Prefs.lang.idx()][m - 1]

    class Event(val month: Int, val day: Int, val name: L)

    val events = listOf(
        Event(1, 1, L("Islamic New Year", "ইসলামী নববর্ষ", "رأس السنة الهجرية")),
        Event(1, 10, L("Day of Ashura", "আশুরা", "يوم عاشوراء")),
        Event(3, 12, L("Mawlid an-Nabi (12 Rabi' al-Awwal)", "ঈদে মিলাদুন্নবী (১২ রবিউল আউয়াল)", "المولد النبوي")),
        Event(7, 27, L("Isra and Mi'raj", "শবে মিরাজ", "الإسراء والمعراج")),
        Event(8, 15, L("Shab-e-Barat (15 Sha'ban)", "শবে বরাত", "ليلة النصف من شعبان")),
        Event(9, 1, L("First day of Ramadan", "রমজানের প্রথম দিন", "أول أيام رمضان")),
        Event(9, 27, L("Laylat al-Qadr (27th night, commonly observed)", "শবে কদর (২৭তম রাত, প্রচলিত)", "ليلة القدر (٢٧، المشهورة)")),
        Event(10, 1, L("Eid al-Fitr", "ঈদুল ফিতর", "عيد الفطر")),
        Event(12, 9, L("Day of Arafah", "আরাফার দিন", "يوم عرفة")),
        Event(12, 10, L("Eid al-Adha", "ঈদুল আযহা", "عيد الأضحى")),
    )
}

val HIJRI_MONTHS = arrayOf(
    arrayOf("Muharram", "Safar", "Rabi' al-Awwal", "Rabi' al-Thani", "Jumada al-Awwal", "Jumada al-Thani", "Rajab", "Sha'ban", "Ramadan", "Shawwal", "Dhu al-Qi'dah", "Dhu al-Hijjah"),
    arrayOf("মুহাররম", "সফর", "রবিউল আউয়াল", "রবিউস সানি", "জমাদিউল আউয়াল", "জমাদিউস সানি", "রজব", "শা'বান", "রমজান", "শাওয়াল", "জিলকদ", "জিলহজ"),
    arrayOf("محرم", "صفر", "ربيع الأول", "ربيع الآخر", "جمادى الأولى", "جمادى الآخرة", "رجب", "شعبان", "رمضان", "شوال", "ذو القعدة", "ذو الحجة"),
)

val GREG_MONTHS = arrayOf(
    arrayOf("January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"),
    arrayOf("জানুয়ারি", "ফেব্রুয়ারি", "মার্চ", "এপ্রিল", "মে", "জুন", "জুলাই", "আগস্ট", "সেপ্টেম্বর", "অক্টোবর", "নভেম্বর", "ডিসেম্বর"),
    arrayOf("يناير", "فبراير", "مارس", "أبريل", "مايو", "يونيو", "يوليو", "أغسطس", "سبتمبر", "أكتوبر", "نوفمبر", "ديسمبر"),
)

/** Sunday first. */
val WEEKDAYS = arrayOf(
    arrayOf("Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"),
    arrayOf("রবিবার", "সোমবার", "মঙ্গলবার", "বুধবার", "বৃহস্পতিবার", "শুক্রবার", "শনিবার"),
    arrayOf("الأحد", "الاثنين", "الثلاثاء", "الأربعاء", "الخميس", "الجمعة", "السبت"),
)

val WEEKDAYS_SHORT = arrayOf(
    arrayOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"),
    arrayOf("রবি", "সোম", "মঙ্গল", "বুধ", "বৃহ", "শুক্র", "শনি"),
    arrayOf("أحد", "اثنين", "ثلاثاء", "أربعاء", "خميس", "جمعة", "سبت"),
)

fun String.idx(): Int = when (this) { "bn" -> 1; "ar" -> 2; else -> 0 }

fun gregorianLong(d: LocalDate): String {
    val wd = WEEKDAYS[Prefs.lang.idx()][d.dayOfWeek.value % 7]
    return nd("$wd, ${d.dayOfMonth} ${GREG_MONTHS[Prefs.lang.idx()][d.monthValue - 1]} ${d.year}")
}
