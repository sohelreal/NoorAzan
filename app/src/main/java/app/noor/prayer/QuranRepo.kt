package app.noor.prayer

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

class Surah(val n: Int, val en: String, val bn: String, val ar: String, val ayahs: Int, val meccan: Boolean)
class Ayah(val ar: String, val en: String, val bn: String)

/**
 * Surah metadata is bundled (works offline). Ayah text (Arabic Uthmani + English Saheeh International +
 * Bengali) is fetched once per surah from api.alquran.cloud and cached in app storage.
 * Full-surah audio streams from cdn.islamic.network.
 */
object QuranRepo {
    val reciters = listOf(
        "ar.alafasy" to "reciter_alafasy",
        "ar.abdurrahmaansudais" to "reciter_sudais",
        "ar.husary" to "reciter_husary",
        "ar.minshawi" to "reciter_minshawi",
        "ar.abdulbasitmurattal" to "reciter_abdulbasit",
    )

    private val servers = mapOf(
        "ar.alafasy" to listOf("https://server8.mp3quran.net/afs/%03d.mp3", "https://download.quranicaudio.com/quran/mishaari_raashid_al_3afaasy/%03d.mp3"),
        "ar.abdurrahmaansudais" to listOf("https://server11.mp3quran.net/sds/%03d.mp3", "https://download.quranicaudio.com/quran/abdurrahmaan_as-sudays/%03d.mp3"),
        "ar.husary" to listOf("https://server13.mp3quran.net/husr/%03d.mp3"),
        "ar.minshawi" to listOf("https://server10.mp3quran.net/minsh/%03d.mp3"),
        "ar.abdulbasitmurattal" to listOf("https://server7.mp3quran.net/basit/%03d.mp3"),
    )

    /** Candidate stream/download URLs for a full Surah; the player tries them in order. */
    fun audioUrls(reciter: String, n: Int): List<String> =
        (servers[reciter] ?: servers.getValue("ar.alafasy")).map { String.format(java.util.Locale.ENGLISH, it, n) } +
            "https://cdn.islamic.network/quran/audio-surah/128/$reciter/$n.mp3"

    fun audioFile(ctx: Context, reciter: String, n: Int) = File(ctx.filesDir, "audio/$reciter/$n.mp3")

    fun audioSaved(ctx: Context, reciter: String, n: Int): Boolean = audioFile(ctx, reciter, n).let { it.exists() && it.length() > 10000 }

    /** Downloads a Surah's audio to app storage so it plays offline and without buffering. */
    suspend fun saveAudio(ctx: Context, reciter: String, n: Int, onProgress: (Float) -> Unit): Boolean = withContext(Dispatchers.IO) {
        val dest = audioFile(ctx, reciter, n)
        dest.parentFile?.mkdirs()
        for (url in audioUrls(reciter, n)) {
            val tmp = File(dest.path + ".part")
            try {
                val c = URL(url).openConnection() as HttpURLConnection
                c.connectTimeout = 15000
                c.readTimeout = 30000
                if (c.responseCode != 200) continue
                val total = c.contentLengthLong
                var done = 0L
                c.inputStream.use { i ->
                    tmp.outputStream().use { o ->
                        val buf = ByteArray(32 * 1024)
                        while (true) {
                            val r = i.read(buf)
                            if (r < 0) break
                            o.write(buf, 0, r)
                            done += r
                            if (total > 0) onProgress(done.toFloat() / total)
                        }
                    }
                }
                if (tmp.length() > 10000 && (total <= 0 || tmp.length() == total)) {
                    dest.delete()
                    if (tmp.renameTo(dest)) return@withContext true
                }
                tmp.delete()
            } catch (e: Exception) {
                tmp.delete()
            }
        }
        false
    }

    private const val RAW = """
1|Al-Fatihah|ফাতিহা|الفاتحة|7|M
2|Al-Baqarah|বাকারা|البقرة|286|D
3|Ali 'Imran|আলে ইমরান|آل عمران|200|D
4|An-Nisa|নিসা|النساء|176|D
5|Al-Ma'idah|মায়িদা|المائدة|120|D
6|Al-An'am|আনআম|الأنعام|165|M
7|Al-A'raf|আ'রাফ|الأعراف|206|M
8|Al-Anfal|আনফাল|الأنفال|75|D
9|At-Tawbah|তাওবা|التوبة|129|D
10|Yunus|ইউনুস|يونس|109|M
11|Hud|হূদ|هود|123|M
12|Yusuf|ইউসুফ|يوسف|111|M
13|Ar-Ra'd|রা'দ|الرعد|43|D
14|Ibrahim|ইবরাহীম|إبراهيم|52|M
15|Al-Hijr|হিজর|الحجر|99|M
16|An-Nahl|নাহল|النحل|128|M
17|Al-Isra|বনী ইসরাঈল|الإسراء|111|M
18|Al-Kahf|কাহফ|الكهف|110|M
19|Maryam|মারইয়াম|مريم|98|M
20|Taha|ত্বোয়া-হা|طه|135|M
21|Al-Anbiya|আম্বিয়া|الأنبياء|112|M
22|Al-Hajj|হজ্জ|الحج|78|D
23|Al-Mu'minun|মু'মিনূন|المؤمنون|118|M
24|An-Nur|নূর|النور|64|D
25|Al-Furqan|ফুরকান|الفرقان|77|M
26|Ash-Shu'ara|শুআরা|الشعراء|227|M
27|An-Naml|নামল|النمل|93|M
28|Al-Qasas|কাসাস|القصص|88|M
29|Al-'Ankabut|আনকাবূত|العنكبوت|69|M
30|Ar-Rum|রূম|الروم|60|M
31|Luqman|লুকমান|لقمان|34|M
32|As-Sajdah|সাজদাহ|السجدة|30|M
33|Al-Ahzab|আহযাব|الأحزاب|73|D
34|Saba|সাবা|سبأ|54|M
35|Fatir|ফাতির|فاطر|45|M
36|Ya-Sin|ইয়াসীন|يس|83|M
37|As-Saffat|সাফফাত|الصافات|182|M
38|Sad|সোয়াদ|ص|88|M
39|Az-Zumar|যুমার|الزمر|75|M
40|Ghafir|গাফির|غافر|85|M
41|Fussilat|ফুসসিলাত|فصلت|54|M
42|Ash-Shura|শূরা|الشورى|53|M
43|Az-Zukhruf|যুখরুফ|الزخرف|89|M
44|Ad-Dukhan|দুখান|الدخان|59|M
45|Al-Jathiyah|জাসিয়া|الجاثية|37|M
46|Al-Ahqaf|আহকাফ|الأحقاف|35|M
47|Muhammad|মুহাম্মাদ|محمد|38|D
48|Al-Fath|ফাতহ|الفتح|29|D
49|Al-Hujurat|হুজুরাত|الحجرات|18|D
50|Qaf|ক্বাফ|ق|45|M
51|Adh-Dhariyat|যারিয়াত|الذاريات|60|M
52|At-Tur|তূর|الطور|49|M
53|An-Najm|নাজম|النجم|62|M
54|Al-Qamar|ক্বামার|القمر|55|M
55|Ar-Rahman|আর-রাহমান|الرحمن|78|D
56|Al-Waqi'ah|ওয়াকিয়াহ|الواقعة|96|M
57|Al-Hadid|হাদীদ|الحديد|29|D
58|Al-Mujadila|মুজাদালাহ|المجادلة|22|D
59|Al-Hashr|হাশর|الحشر|24|D
60|Al-Mumtahanah|মুমতাহিনাহ|الممتحنة|13|D
61|As-Saff|সাফ|الصف|14|D
62|Al-Jumu'ah|জুমুআ|الجمعة|11|D
63|Al-Munafiqun|মুনাফিকূন|المنافقون|11|D
64|At-Taghabun|তাগাবুন|التغابن|18|D
65|At-Talaq|তালাক|الطلاق|12|D
66|At-Tahrim|তাহরীম|التحريم|12|D
67|Al-Mulk|মুলক|الملك|30|M
68|Al-Qalam|কলম|القلم|52|M
69|Al-Haqqah|হাক্কাহ|الحاقة|52|M
70|Al-Ma'arij|মা'আরিজ|المعارج|44|M
71|Nuh|নূহ|نوح|28|M
72|Al-Jinn|জিন|الجن|28|M
73|Al-Muzzammil|মুযযাম্মিল|المزمل|20|M
74|Al-Muddaththir|মুদ্দাসসির|المدثر|56|M
75|Al-Qiyamah|কিয়ামাহ|القيامة|40|M
76|Al-Insan|ইনসান|الإنسان|31|D
77|Al-Mursalat|মুরসালাত|المرسلات|50|M
78|An-Naba|নাবা|النبأ|40|M
79|An-Nazi'at|নাযিয়াত|النازعات|46|M
80|'Abasa|আবাসা|عبس|42|M
81|At-Takwir|তাকবীর|التكوير|29|M
82|Al-Infitar|ইনফিতার|الانفطار|19|M
83|Al-Mutaffifin|মুতাফফিফীন|المطففين|36|M
84|Al-Inshiqaq|ইনশিকাক|الانشقاق|25|M
85|Al-Buruj|বুরূজ|البروج|22|M
86|At-Tariq|তারিক|الطارق|17|M
87|Al-A'la|আ'লা|الأعلى|19|M
88|Al-Ghashiyah|গাশিয়াহ|الغاشية|26|M
89|Al-Fajr|ফজর|الفجر|30|M
90|Al-Balad|বালাদ|البلد|20|M
91|Ash-Shams|শামস|الشمس|15|M
92|Al-Layl|লাইল|الليل|21|M
93|Ad-Duha|দুহা|الضحى|11|M
94|Ash-Sharh|ইনশিরাহ|الشرح|8|M
95|At-Tin|তীন|التين|8|M
96|Al-'Alaq|আলাক|العلق|19|M
97|Al-Qadr|কদর|القدر|5|M
98|Al-Bayyinah|বাইয়্যিনাহ|البينة|8|D
99|Az-Zalzalah|যিলযাল|الزلزلة|8|D
100|Al-'Adiyat|আদিয়াত|العاديات|11|M
101|Al-Qari'ah|কারিআহ|القارعة|11|M
102|At-Takathur|তাকাসুর|التكاثر|8|M
103|Al-'Asr|আসর|العصر|3|M
104|Al-Humazah|হুমাযাহ|الهمزة|9|M
105|Al-Fil|ফীল|الفيل|5|M
106|Quraysh|কুরাইশ|قريش|4|M
107|Al-Ma'un|মাউন|الماعون|7|M
108|Al-Kawthar|কাউসার|الكوثر|3|M
109|Al-Kafirun|কাফিরূন|الكافرون|6|M
110|An-Nasr|নাসর|النصر|3|D
111|Al-Masad|লাহাব|المسد|5|M
112|Al-Ikhlas|ইখলাস|الإخلاص|4|M
113|Al-Falaq|ফালাক|الفلق|5|M
114|An-Nas|নাস|الناس|6|M
"""

    val surahs: List<Surah> = RAW.trim().lines().map { line ->
        val p = line.trim().split("|")
        Surah(p[0].toInt(), p[1], p[2], p[3], p[4].toInt(), p[5] == "M")
    }

    private fun base(s: String) = s.filter { it !in '\u064B'..'\u065F' && it != '\u0670' && it !in '\u06D6'..'\u06ED' && it != '\u0640' }

    /** The API's Uthmani text prefixes ayah 1 of most surahs with the Bismillah; we show it separately. */
    private fun stripBasmala(t: String): String {
        val parts = t.trim().split(" ")
        return if (parts.size > 4 && base(parts[0]) == "بسم") parts.drop(4).joinToString(" ") else t
    }

    private fun ayahsOf(data: JSONArray, edition: String, fallbackIndex: Int): JSONArray {
        for (i in 0 until data.length()) {
            val o = data.getJSONObject(i)
            if (o.optJSONObject("edition")?.optString("identifier") == edition) return o.getJSONArray("ayahs")
        }
        return data.getJSONObject(fallbackIndex).getJSONArray("ayahs")
    }

    private fun parse(json: String, n: Int): List<Ayah>? = try {
        val data = JSONObject(json).getJSONArray("data")
        if (data.length() < 3) null else {
            val ar = ayahsOf(data, "quran-uthmani", 0)
            val en = ayahsOf(data, "en.sahih", 1)
            val bn = ayahsOf(data, "bn.bengali", 2)
            List(ar.length()) { i ->
                var t = ar.getJSONObject(i).getString("text")
                if (n != 1 && n != 9 && i == 0) t = stripBasmala(t)
                Ayah(t, en.getJSONObject(i).getString("text"), bn.getJSONObject(i).getString("text"))
            }
        }
    } catch (e: Exception) {
        null
    }

    private fun http(url: String): String? = try {
        val c = URL(url).openConnection() as HttpURLConnection
        c.connectTimeout = 15000
        c.readTimeout = 30000
        if (c.responseCode == 200) c.inputStream.bufferedReader().use { it.readText() } else null
    } catch (e: Exception) {
        null
    }

    private fun cacheFile(ctx: Context, n: Int) = File(ctx.filesDir, "quran/$n.json")

    fun isCached(ctx: Context, n: Int) = cacheFile(ctx, n).exists()

    fun cachedCount(ctx: Context): Int = File(ctx.filesDir, "quran").list()?.size ?: 0

    suspend fun load(ctx: Context, n: Int): List<Ayah>? = withContext(Dispatchers.IO) {
        val f = cacheFile(ctx, n)
        if (f.exists()) {
            val cached = parse(f.readText(), n)
            if (cached != null) return@withContext cached
        }
        val body = http("https://api.alquran.cloud/v1/surah/$n/editions/quran-uthmani,en.sahih,bn.bengali")
            ?: return@withContext null
        val list = parse(body, n) ?: return@withContext null
        f.parentFile?.mkdirs()
        f.writeText(body)
        list
    }
}
