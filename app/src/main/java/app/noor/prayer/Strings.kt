package app.noor.prayer

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/** A trilingual text: English, Bangla, Arabic. */
class L(val en: String, val bn: String, val ar: String) {
    fun s(): String = when (Prefs.lang) { "bn" -> bn; "ar" -> ar; else -> en }
}

/** Converts ASCII digits to the digits of the current UI language. */
fun nd(s: String): String = when (Prefs.lang) {
    "bn" -> s.map { if (it in '0'..'9') ('০' + (it - '0')) else it }.joinToString("")
    "ar" -> s.map { if (it in '0'..'9') ('٠' + (it - '0')) else it }.joinToString("")
    else -> s
}

fun fmtTime(ms: Long): String {
    val pattern = if (Prefs.use24) "HH:mm" else "hh:mm a"
    var s = DateTimeFormatter.ofPattern(pattern, Locale.ENGLISH).format(Instant.ofEpochMilli(ms).atZone(ZoneId.systemDefault()))
    if (Prefs.lang == "ar") s = s.replace("AM", "ص").replace("PM", "م")
    if (Prefs.lang == "bn") s = s.replace("AM", "পূর্বাহ্ণ").replace("PM", "অপরাহ্ণ")
    return nd(s)
}

fun fmtCountdown(ms: Long): String {
    val t = (ms / 1000).coerceAtLeast(0)
    val h = t / 3600
    val m = (t % 3600) / 60
    val s = t % 60
    return nd("%02d:%02d:%02d".format(Locale.ENGLISH, h, m, s))
}

private val m = HashMap<String, Array<String>>()
private fun a(k: String, en: String, bn: String, ar: String) { m[k] = arrayOf(en, bn, ar) }

/** Translate a UI key into the current language. Reads Prefs.lang so Compose recomposes on change. */
fun tr(key: String): String = m[key]?.get(Prefs.lang.idx()) ?: key

val PRAYER_KEYS = listOf("fajr", "dhuhr", "asr", "maghrib", "isha")

fun prayerName(p: Int, jumuah: Boolean = false): String =
    if (p == 1 && jumuah) tr("jumuah") else tr(PRAYER_KEYS[p])

private val stringsInit = run {
    a("save_audio", "Save audio offline", "অডিও অফলাইনে সেভ করুন", "حفظ الصوت للاستماع دون إنترنت")
    a("audio_saved", "Audio saved – plays offline", "অডিও সেভ হয়েছে – অফলাইনে চলবে", "تم حفظ الصوت – يعمل دون إنترنت")
    a("saving", "Saving", "সেভ হচ্ছে", "جارٍ الحفظ")
    a("app_name", "Noor – Azan & Prayer", "নূর – আযান ও নামাজ", "نور – الأذان والصلاة")
    a("home", "Home", "হোম", "الرئيسية")
    a("quran", "Quran", "কুরআন", "القرآن")
    a("salah", "Salah", "নামাজ", "الصلاة")
    a("qibla", "Qibla", "কিবলা", "القبلة")
    a("more", "More", "আরও", "المزيد")
    a("calendar", "Islamic Calendar", "ইসলামী ক্যালেন্ডার", "التقويم الهجري")
    a("ramadan", "Ramadan Calendar", "রমজান ক্যালেন্ডার", "تقويم رمضان")
    a("settings", "Settings", "সেটিংস", "الإعدادات")
    a("fajr", "Fajr", "ফজর", "الفجر")
    a("sunrise", "Sunrise", "সূর্যোদয়", "الشروق")
    a("dhuhr", "Dhuhr", "যোহর", "الظهر")
    a("jumuah", "Jumu'ah", "জুমুআ", "الجمعة")
    a("asr", "Asr", "আসর", "العصر")
    a("maghrib", "Maghrib", "মাগরিব", "المغرب")
    a("isha", "Isha", "ইশা", "العشاء")
    a("next_prayer", "Next prayer", "পরবর্তী ওয়াক্ত", "الصلاة القادمة")
    a("remaining", "Time remaining", "বাকি সময়", "الوقت المتبقي")
    a("loc_not_set", "Location not set. Tap to detect with GPS.", "লোকেশন সেট করা নেই। জিপিএসে শনাক্ত করতে ট্যাপ করুন।", "لم يتم تحديد الموقع. اضغط للتحديد عبر GPS.")
    a("detect", "Detect location", "লোকেশন শনাক্ত করুন", "تحديد الموقع")
    a("exact_alarm", "Allow exact alarms so Azan plays on time", "সঠিক সময়ে আযানের জন্য এক্স্যাক্ট অ্যালার্মের অনুমতি দিন", "اسمح بالمنبهات الدقيقة ليعمل الأذان في وقته")
    a("allow", "Allow", "অনুমতি দিন", "السماح")
    a("notif_perm", "Allow notifications for Azan alerts", "আযানের নোটিফিকেশনের অনুমতি দিন", "اسمح بالإشعارات لتنبيهات الأذان")
    a("quick_access", "Quick access", "দ্রুত অ্যাক্সেস", "وصول سريع")
    a("continue_reading", "Continue reading", "পড়া চালিয়ে যান", "متابعة القراءة")
    a("its_time", "It is time for %s", "%s এর সময় হয়েছে", "حان وقت %s")
    a("stop", "Stop", "থামান", "إيقاف")
    a("back", "Back", "পেছনে", "رجوع")
    a("today", "Today", "আজ", "اليوم")
    a("prayer_times", "Prayer times", "নামাজের সময়", "مواقيت الصلاة")
    // Quran
    a("search_surah", "Search surah", "সূরা খুঁজুন", "ابحث عن سورة")
    a("ayahs", "ayahs", "আয়াত", "آيات")
    a("meccan", "Meccan", "মক্কী", "مكية")
    a("medinan", "Medinan", "মাদানী", "مدنية")
    a("download_all", "Download all for offline", "অফলাইনের জন্য সব ডাউনলোড", "تنزيل الكل للاستخدام دون اتصال")
    a("downloading", "Downloading", "ডাউনলোড হচ্ছে", "جارٍ التنزيل")
    a("saved_offline", "Saved offline", "অফলাইনে সংরক্ষিত", "محفوظ دون اتصال")
    a("loading", "Loading…", "লোড হচ্ছে…", "جارٍ التحميل…")
    a("no_internet", "Could not load. Check your internet connection.", "লোড করা যায়নি। ইন্টারনেট সংযোগ পরীক্ষা করুন।", "تعذر التحميل. تحقق من الاتصال بالإنترنت.")
    a("retry", "Retry", "আবার চেষ্টা", "إعادة المحاولة")
    a("play_surah", "Play full Surah", "পুরো সূরা শুনুন", "تشغيل السورة كاملة")
    a("pause", "Pause", "বিরতি", "إيقاف مؤقت")
    a("resume", "Resume", "চালু করুন", "استئناف")
    a("reciter", "Reciter", "ক্বারী", "القارئ")
    a("show_translation", "Show translation", "অনুবাদ দেখান", "إظهار الترجمة")
    a("arabic_size", "Arabic text size", "আরবি লেখার আকার", "حجم الخط العربي")
    a("audio_error", "Audio failed. Check your internet connection.", "অডিও চালানো যায়নি। ইন্টারনেট সংযোগ দেখুন।", "فشل تشغيل الصوت. تحقق من الإنترنت.")
    a("reciter_alafasy", "Mishary Alafasy", "মিশারি আলআফাসি", "مشاري العفاسي")
    a("reciter_sudais", "Abdurrahman As-Sudais", "আবদুর রহমান আস-সুদাইস", "عبد الرحمن السديس")
    a("reciter_husary", "Mahmoud Al-Husary", "মাহমুদ খলিল আল-হুসারি", "محمود خليل الحصري")
    a("reciter_minshawi", "Mohamed Al-Minshawi", "মুহাম্মদ আল-মিনশাভি", "محمد صديق المنشاوي")
    a("reciter_abdulbasit", "Abdul Basit (Murattal)", "আবদুল বাসিত (মুরাত্তাল)", "عبد الباسط (مرتل)")
    // Salah guide
    a("prayers_tab", "Prayers", "ওয়াক্ত", "الصلوات")
    a("how_tab", "How to pray", "পদ্ধতি", "الكيفية")
    a("duas_tab", "Duas", "দোয়া", "الأدعية")
    a("rakat", "rakat", "রাকআত", "ركعات")
    a("before", "before", "আগে", "قبل")
    a("after", "after", "পরে", "بعد")
    a("niyat", "Niyat (intention)", "নিয়ত", "النية")
    a("tap_niyat", "Tap a prayer to see Niyat", "নিয়ত দেখতে নামাজে ট্যাপ করুন", "اضغط على الصلاة لعرض النية")
    a("madhab_note", "Rakat counts follow common Hanafi practice; other schools differ slightly. Consult a scholar for rulings.", "রাকআতের সংখ্যা প্রচলিত হানাফী মাযহাব অনুযায়ী; অন্য মাযহাবে সামান্য পার্থক্য আছে। মাসআলার জন্য আলেমের পরামর্শ নিন।", "عدد الركعات وفق المذهب الحنفي الشائع؛ وتختلف المذاهب قليلاً. راجع أهل العلم.")
    a("wudu_title", "Wudu (Ablution)", "ওযু", "الوضوء")
    a("steps_title", "Steps of Salah", "নামাজের ধাপসমূহ", "خطوات الصلاة")
    // Qibla
    a("qibla_dir", "Qibla direction", "কিবলার দিক", "اتجاه القبلة")
    a("facing", "You are facing the Qibla", "আপনি কিবলামুখী", "أنت تواجه القبلة")
    a("keep_flat", "Hold your phone flat, away from metal and magnets. Move in a figure-8 to calibrate.", "ফোন সমতলে ধরুন, ধাতু ও চুম্বক থেকে দূরে। ক্যালিব্রেট করতে ৮-এর মতো ঘোরান।", "امسك الهاتف أفقياً بعيداً عن المعادن. حركه على شكل ٨ للمعايرة.")
    a("no_sensor", "Compass sensor not available on this device.", "এই ডিভাইসে কম্পাস সেন্সর নেই।", "مستشعر البوصلة غير متوفر.")
    a("from_north", "from North", "উত্তর থেকে", "من الشمال")
    // Calendar / Ramadan
    a("events", "Islamic events", "ইসলামী দিবস", "المناسبات الإسلامية")
    a("sehri_ends", "Sehri ends", "সেহরি শেষ", "نهاية السحور")
    a("iftar", "Iftar", "ইফতার", "الإفطار")
    a("sehri", "Sehri", "সেহরি", "السحور")
    a("day", "Day", "দিন", "اليوم")
    a("date", "Date", "তারিখ", "التاريخ")
    a("starts_in", "Ramadan starts in", "রমজান শুরু হতে বাকি", "يبدأ رمضان بعد")
    a("days", "days", "দিন", "أيام")
    a("ramadan_day", "Ramadan day", "রমজানের দিন", "يوم رمضان")
    a("ramadan_note", "Ramadan start depends on moon sighting. Use Settings > Hijri date adjustment (-2 to +2) to match your local announcement.", "রমজানের শুরু চাঁদ দেখার ওপর নির্ভরশীল। স্থানীয় ঘোষণার সাথে মেলাতে সেটিংসে হিজরি তারিখ সমন্বয় (-২ থেকে +২) ব্যবহার করুন।", "يعتمد بدء رمضان على رؤية الهلال. استخدم تعديل التاريخ الهجري في الإعدادات (‎-٢ إلى ‎+٢).")
    a("sehri_dua", "Sehri intention", "সেহরির নিয়ত", "نية السحور")
    a("iftar_dua", "Iftar dua", "ইফতারের দোয়া", "دعاء الإفطار")
    // Settings
    a("language", "Language", "ভাষা", "اللغة")
    a("theme", "Theme", "থিম", "المظهر")
    a("system", "System", "সিস্টেম", "النظام")
    a("light", "Light", "লাইট", "فاتح")
    a("dark", "Dark", "ডার্ক", "داكن")
    a("calc_method", "Calculation method", "গণনা পদ্ধতি", "طريقة الحساب")
    a("method_MWL", "Muslim World League", "মুসলিম ওয়ার্ল্ড লীগ", "رابطة العالم الإسلامي")
    a("method_ISNA", "ISNA (North America)", "ISNA (উত্তর আমেরিকা)", "أمريكا الشمالية (ISNA)")
    a("method_EGYPT", "Egyptian General Authority", "মিশরীয় সাধারণ কর্তৃপক্ষ", "الهيئة المصرية العامة")
    a("method_MAKKAH", "Umm al-Qura (Makkah)", "উম্মুল কুরা (মক্কা)", "أم القرى (مكة)")
    a("method_KARACHI", "Univ. of Islamic Sciences, Karachi", "করাচি ইসলামী বিজ্ঞান বিশ্ববিদ্যালয়", "جامعة العلوم الإسلامية، كراتشي")
    a("asr_method", "Asr calculation", "আসরের হিসাব", "حساب العصر")
    a("standard", "Standard (Shafi'i, Maliki, Hanbali)", "সাধারণ (শাফেয়ী, মালেকী, হাম্বলী)", "قياسي (الشافعي والمالكي والحنبلي)")
    a("hanafi", "Hanafi", "হানাফী", "حنفي")
    a("adjust_min", "Fine-tune minutes (+/-)", "মিনিট সমন্বয় (+/-)", "ضبط الدقائق (+/-)")
    a("notifications", "Azan & notifications", "আযান ও নোটিফিকেশন", "الأذان والإشعارات")
    a("mode_azan", "Play Azan audio", "আযান অডিও বাজান", "تشغيل صوت الأذان")
    a("mode_notif", "Notification sound only", "শুধু নোটিফিকেশন সাউন্ড", "صوت الإشعار فقط")
    a("mode_silent", "Silent notification", "নীরব নোটিফিকেশন", "إشعار صامت")
    a("choose_file", "Choose Azan audio file", "আযান অডিও ফাইল বাছাই করুন", "اختيار ملف صوت الأذان")
    a("download_azan", "Download Azan audio", "আযান অডিও ডাউনলোড", "تنزيل صوت الأذان")
    a("azan_url", "Azan audio URL (https)", "আযান অডিও ইউআরএল (https)", "رابط صوت الأذان (https)")
    a("test_azan", "Test Azan", "আযান পরীক্ষা", "تجربة الأذان")
    a("azan_saved", "Azan audio saved on device", "আযান অডিও ডিভাইসে সংরক্ষিত", "تم حفظ صوت الأذان على الجهاز")
    a("azan_none", "No saved audio; it will be streamed at prayer time (internet needed) or the system sound is used.", "সংরক্ষিত অডিও নেই; ওয়াক্তের সময় ইন্টারনেট থেকে চলবে অথবা সিস্টেম সাউন্ড বাজবে।", "لا يوجد صوت محفوظ؛ سيتم البث عند الوقت أو استخدام صوت النظام.")
    a("remove_azan", "Remove saved audio", "সংরক্ষিত অডিও মুছুন", "حذف الصوت المحفوظ")
    a("time_format", "24-hour time", "২৪ ঘণ্টার সময়", "نظام ٢٤ ساعة")
    a("hijri_adj", "Hijri date adjustment (days)", "হিজরি তারিখ সমন্বয় (দিন)", "تعديل التاريخ الهجري (أيام)")
    a("manual_loc", "Manual location", "ম্যানুয়াল লোকেশন", "الموقع يدوياً")
    a("latitude", "Latitude", "অক্ষাংশ", "خط العرض")
    a("longitude", "Longitude", "দ্রাঘিমাংশ", "خط الطول")
    a("save", "Save", "সংরক্ষণ", "حفظ")
    a("invalid", "Invalid value", "অবৈধ মান", "قيمة غير صالحة")
    a("quran_settings", "Quran", "কুরআন", "القرآن")
    a("about", "About", "সম্পর্কে", "حول")
    a("about_text", "100% ad-free. No login. No subscriptions. Prayer times are calculated on your device. Quran text is downloaded once per surah and kept offline.", "১০০% বিজ্ঞাপনমুক্ত। লগইন নেই। সাবস্ক্রিপশন নেই। নামাজের সময় আপনার ডিভাইসেই হিসাব হয়। কুরআনের টেক্সট প্রতি সূরা একবার ডাউনলোড হয়ে অফলাইনে থাকে।", "بلا إعلانات 100٪. بلا تسجيل دخول. بلا اشتراكات. تُحسب المواقيت على جهازك. يُنزَّل نص القرآن مرة لكل سورة ويحفظ دون اتصال.")
    a("battery_tip", "Tip: exclude this app from battery optimisation so Azan is never delayed.", "পরামর্শ: আযান যেন দেরি না হয়, তাই এই অ্যাপকে ব্যাটারি অপ্টিমাইজেশন থেকে বাদ দিন।", "نصيحة: استثنِ التطبيق من توفير البطارية حتى لا يتأخر الأذان.")
    a("location", "Location", "লোকেশন", "الموقع")
    a("ok", "OK", "ঠিক আছে", "حسناً")
}
