package app.noor.prayer

/** Rakat types: 0 Fard, 1 Sunnah Mu'akkadah, 2 Sunnah Ghayr Mu'akkadah, 3 Nafl, 4 Witr (Wajib). */
val TYPE_NAMES = listOf(
    L("Fard", "ফরজ", "فرض"),
    L("Sunnah Mu'akkadah", "সুন্নতে মুয়াক্কাদাহ", "سنة مؤكدة"),
    L("Sunnah Ghayr Mu'akkadah", "সুন্নতে গায়রে মুয়াক্কাদাহ", "سنة غير مؤكدة"),
    L("Nafl", "নফল", "نافلة"),
    L("Witr (Wajib)", "বিতর (ওয়াজিব)", "الوتر (واجب)"),
)

/** when: 0 before the Fard, 1 after the Fard, 2 the Fard itself. */
class PRow(val type: Int, val rakat: Int, val whenIdx: Int)

class PrayerInfo(val name: L, val rows: List<PRow>)

val PRAYER_INFO = listOf(
    PrayerInfo(L("Fajr", "ফজর", "الفجر"), listOf(PRow(1, 2, 0), PRow(0, 2, 2))),
    PrayerInfo(L("Dhuhr", "যোহর", "الظهر"), listOf(PRow(1, 4, 0), PRow(0, 4, 2), PRow(1, 2, 1), PRow(3, 2, 1))),
    PrayerInfo(L("Asr", "আসর", "العصر"), listOf(PRow(2, 4, 0), PRow(0, 4, 2))),
    PrayerInfo(L("Maghrib", "মাগরিব", "المغرب"), listOf(PRow(0, 3, 2), PRow(1, 2, 1), PRow(3, 2, 1))),
    PrayerInfo(L("Isha", "ইশা", "العشاء"), listOf(PRow(2, 4, 0), PRow(0, 4, 2), PRow(1, 2, 1), PRow(3, 2, 1), PRow(4, 3, 1), PRow(3, 2, 1))),
    PrayerInfo(L("Jumu'ah (Friday)", "জুমুআ (শুক্রবার)", "الجمعة"), listOf(PRow(1, 4, 0), PRow(0, 2, 2), PRow(1, 4, 1), PRow(1, 2, 1), PRow(3, 2, 1))),
)

fun whenLabel(w: Int): String = when (w) { 0 -> tr("before"); 1 -> tr("after"); else -> "" }

fun niyatFor(p: PrayerInfo, r: PRow): String {
    val type = TYPE_NAMES[r.type]
    return when (Prefs.lang) {
        "bn" -> "আমি কিবলামুখী হয়ে একমাত্র আল্লাহর উদ্দেশ্যে ${p.name.bn}-এর ${r.rakat} রাকআত ${type.bn} নামাজ আদায়ের নিয়ত করছি। আল্লাহু আকবার।"
        "ar" -> "نويت أن أصلي لله تعالى ${r.rakat} ركعات ${type.ar} من ${p.name.ar} مستقبلاً القبلة، الله أكبر."
        else -> "I intend to pray ${r.rakat} rakats of ${type.en} for ${p.name.en}, solely for the sake of Allah, facing the Qibla. Allahu Akbar."
    }
}

class Step(val title: L, val detail: L)

val WUDU_STEPS = listOf(
    Step(L("Intention & Bismillah", "নিয়ত ও বিসমিল্লাহ", "النية والبسملة"), L("Make the intention in your heart and say \"Bismillah\".", "অন্তরে নিয়ত করুন এবং \"বিসমিল্লাহ\" বলুন।", "انوِ بقلبك وقل «بسم الله».")),
    Step(L("Wash hands", "হাত ধোয়া", "غسل اليدين"), L("Wash both hands up to the wrists three times.", "দুই হাত কবজি পর্যন্ত তিনবার ধুন।", "اغسل كفيك إلى الرسغين ثلاثاً.")),
    Step(L("Rinse mouth & nose", "কুলি ও নাকে পানি", "المضمضة والاستنشاق"), L("Rinse the mouth and clean the nose three times each.", "তিনবার কুলি করুন ও নাকে পানি দিন।", "تمضمض واستنشق ثلاثاً.")),
    Step(L("Wash face", "মুখ ধোয়া", "غسل الوجه"), L("Wash the whole face three times, from hairline to chin and ear to ear.", "কপালের চুলের গোড়া থেকে থুতনি এবং কান থেকে কান পর্যন্ত তিনবার ধুন।", "اغسل وجهك كاملاً ثلاثاً.")),
    Step(L("Wash arms", "হাত কনুইসহ ধোয়া", "غسل الذراعين"), L("Wash the right then left arm up to and including the elbows, three times.", "ডান ও বাম হাত কনুইসহ তিনবার করে ধুন।", "اغسل يديك مع المرفقين ثلاثاً، اليمنى ثم اليسرى.")),
    Step(L("Wipe head & ears", "মাথা ও কান মাসেহ", "مسح الرأس والأذنين"), L("Wipe the head once with wet hands, then the ears.", "ভেজা হাতে একবার মাথা ও কান মাসেহ করুন।", "امسح رأسك وأذنيك مرة واحدة.")),
    Step(L("Wash feet", "পা ধোয়া", "غسل القدمين"), L("Wash the right then left foot including the ankles, three times, between the toes too.", "ডান ও বাম পা টাখনুসহ তিনবার ধুন, আঙুলের ফাঁকও।", "اغسل قدميك مع الكعبين ثلاثاً.")),
)

val SALAH_STEPS = listOf(
    Step(L("Prepare", "প্রস্তুতি", "التهيؤ"), L("Be in wudu, wear clean clothes, cover the awrah and face the Qibla.", "ওযু করুন, পাক পোশাক পরুন, সতর ঢাকুন এবং কিবলামুখী হোন।", "توضأ والبس ثياباً طاهرة واستقبل القبلة.")),
    Step(L("Niyat", "নিয়ত", "النية"), L("Intend in your heart the prayer you are about to pray.", "অন্তরে যে নামাজ পড়বেন তার নিয়ত করুন।", "انوِ الصلاة التي ستؤديها بقلبك.")),
    Step(L("Takbir Tahrima", "তাকবীরে তাহরীমা", "تكبيرة الإحرام"), L("Raise hands to the ears and say \"Allahu Akbar\". Place the right hand over the left on the chest/navel (per your madhab).", "দুই হাত কান পর্যন্ত তুলে \"আল্লাহু আকবার\" বলুন। ডান হাত বাম হাতের ওপর বাঁধুন।", "ارفع يديك وقل «الله أكبر» ثم ضع اليمنى على اليسرى.")),
    Step(L("Qiyam (standing)", "কিয়াম (দাঁড়ানো)", "القيام"), L("Recite Thana, Ta'awwudh, Bismillah, Surah Al-Fatiha and another surah or verses (first two rakats).", "ছানা, তাআউয, বিসমিল্লাহ, সূরা ফাতিহা এবং আরেকটি সূরা বা কিছু আয়াত পড়ুন (প্রথম দুই রাকআতে)।", "اقرأ الثناء والاستعاذة والبسملة والفاتحة وسورة (في الركعتين الأوليين).")),
    Step(L("Ruku (bowing)", "রুকু", "الركوع"), L("Say \"Allahu Akbar\", bow with a straight back and say \"Subhana Rabbiyal Azim\" three times.", "\"আল্লাহু আকবার\" বলে রুকুতে যান, পিঠ সোজা রেখে তিনবার \"সুবহানা রাব্বিয়াল আযীম\" বলুন।", "كبّر واركع وقل «سبحان ربي العظيم» ثلاثاً.")),
    Step(L("Qawmah", "কাওমা", "القومة"), L("Rise saying \"Sami'allahu liman hamidah\", then \"Rabbana lakal hamd\".", "উঠে \"সামি'আল্লাহু লিমান হামিদাহ\" ও \"রাব্বানা লাকাল হামদ\" বলুন।", "ارفع وقل «سمع الله لمن حمده، ربنا ولك الحمد».")),
    Step(L("Sujood (prostration)", "সিজদা", "السجود"), L("Say \"Allahu Akbar\", prostrate with forehead, nose, palms, knees and toes down; say \"Subhana Rabbiyal A'la\" three times.", "\"আল্লাহু আকবার\" বলে সিজদায় যান; তিনবার \"সুবহানা রাব্বিয়াল আ'লা\" বলুন।", "كبّر واسجد وقل «سبحان ربي الأعلى» ثلاثاً.")),
    Step(L("Jalsa (sitting)", "জলসা (বসা)", "الجلسة"), L("Sit calmly between the two sujood and say \"Rabbighfir li\".", "দুই সিজদার মাঝে স্থির হয়ে বসে \"রাব্বিগফিরলী\" বলুন।", "اجلس مطمئناً وقل «رب اغفر لي».")),
    Step(L("Second sujood & next rakat", "দ্বিতীয় সিজদা ও পরবর্তী রাকআত", "السجدة الثانية والركعة التالية"), L("Make the second sujood, then stand for the next rakat. Repeat the same cycle.", "দ্বিতীয় সিজদা করে পরবর্তী রাকআতের জন্য দাঁড়ান এবং একইভাবে আদায় করুন।", "اسجد السجدة الثانية ثم قم للركعة التالية.")),
    Step(L("Tashahhud", "তাশাহহুদ", "التشهد"), L("In the sitting after two rakats (and the last), recite At-Tahiyyat. In the final sitting add Durood and a dua.", "দুই রাকআতের পর (এবং শেষ বৈঠকে) আত্তাহিয়্যাতু পড়ুন; শেষ বৈঠকে দরূদ ও দোয়া পড়ুন।", "اقرأ التحيات، وفي الأخير الصلاة الإبراهيمية ودعاء.")),
    Step(L("Salam", "সালাম", "التسليم"), L("Turn the face right then left saying \"As-salamu alaykum wa rahmatullah\".", "ডানে ও বাঁয়ে মুখ ফিরিয়ে \"আসসালামু আলাইকুম ওয়া রাহমাতুল্লাহ\" বলুন।", "سلّم يميناً ويساراً «السلام عليكم ورحمة الله».")),
    Step(L("Dhikr & dua", "যিকির ও দোয়া", "الذكر والدعاء"), L("Say Astaghfirullah three times, then Tasbih (33), Tahmid (33), Takbir (34) and make dua.", "তিনবার ইস্তিগফার, তারপর তাসবীহ (৩৩), তাহমীদ (৩৩), তাকবীর (৩৪) এবং দোয়া করুন।", "استغفر ثلاثاً ثم سبّح ٣٣ واحمد ٣٣ وكبّر ٣٤ وادعُ.")),
)

class Dua(val title: L, val arabic: String, val translit: L, val meaning: L)

val DUAS = listOf(
    Dua(
        L("Dua after Azan", "আযানের পরের দোয়া", "دعاء بعد الأذان"),
        "اللَّهُمَّ رَبَّ هَذِهِ الدَّعْوَةِ التَّامَّةِ وَالصَّلَاةِ الْقَائِمَةِ آتِ مُحَمَّدًا الْوَسِيلَةَ وَالْفَضِيلَةَ وَابْعَثْهُ مَقَامًا مَحْمُودًا الَّذِي وَعَدْتَهُ",
        L("Allahumma Rabba hadhihid-da'watit-tammah, was-salatil-qa'imah, ati Muhammadanil-wasilata wal-fadilah, wab'athhu maqamam-mahmudanil-ladhi wa'adtah.",
            "আল্লাহুম্মা রাব্বা হাযিহিদ দা'ওয়াতিত তাম্মাহ, ওয়াস সালাতিল ক্বায়িমাহ, আতি মুহাম্মাদানিল ওয়াসীলাতা ওয়াল ফাদীলাহ, ওয়াব'আসহু মাক্বামাম মাহমূদানিল্লাযী ওয়া'আদতাহ।",
            "Allahumma Rabba hadhihid-da'watit-tammah, was-salatil-qa'imah, ati Muhammadanil-wasilata wal-fadilah, wab'athhu maqamam-mahmudanil-ladhi wa'adtah."),
        L("O Allah, Lord of this perfect call and established prayer, grant Muhammad (peace be upon him) the Wasilah and Fadilah and raise him to the praised station You promised him.",
            "হে আল্লাহ! এই পূর্ণাঙ্গ আহ্বান ও প্রতিষ্ঠিত নামাজের রব, মুহাম্মাদ (সা.)-কে ওয়াসীলা ও ফযীলত দান করুন এবং তাঁকে সেই প্রশংসিত স্থানে পৌঁছান যার ওয়াদা আপনি করেছেন।",
            "اللهم رب هذه الدعوة التامة والصلاة القائمة، أعطِ محمداً ﷺ الوسيلة والفضيلة وابعثه المقام المحمود الذي وعدته.")
    ),
    Dua(
        L("Before Wudu", "ওযুর আগে", "قبل الوضوء"),
        "بِسْمِ اللَّهِ",
        L("Bismillah", "বিসমিল্লাহ", "Bismillah"),
        L("In the name of Allah.", "আল্লাহর নামে (শুরু করছি)।", "بسم الله.")
    ),
    Dua(
        L("After Wudu", "ওযুর পরে", "بعد الوضوء"),
        "أَشْهَدُ أَنْ لَا إِلَٰهَ إِلَّا اللَّهُ وَحْدَهُ لَا شَرِيكَ لَهُ وَأَشْهَدُ أَنَّ مُحَمَّدًا عَبْدُهُ وَرَسُولُهُ، اللَّهُمَّ اجْعَلْنِي مِنَ التَّوَّابِينَ وَاجْعَلْنِي مِنَ الْمُتَطَهِّرِينَ",
        L("Ashhadu al-la ilaha illallahu wahdahu la sharika lah, wa ashhadu anna Muhammadan 'abduhu wa rasuluh. Allahummaj'alni minat-tawwabina waj'alni minal-mutatahhirin.",
            "আশহাদু আল্লা ইলাহা ইল্লাল্লাহু ওয়াহদাহু লা শারীকা লাহু, ওয়া আশহাদু আন্না মুহাম্মাদান আবদুহু ওয়া রাসূলুহু। আল্লাহুম্মাজ'আলনী মিনাত তাওওয়াবীনা ওয়াজ'আলনী মিনাল মুতাত্বাহহিরীন।",
            "Ashhadu al-la ilaha illallahu wahdahu la sharika lah, wa ashhadu anna Muhammadan 'abduhu wa rasuluh. Allahummaj'alni minat-tawwabina waj'alni minal-mutatahhirin."),
        L("I bear witness that there is no god but Allah alone, without partner, and that Muhammad is His servant and messenger. O Allah, make me among those who repent and among those who purify themselves.",
            "আমি সাক্ষ্য দিচ্ছি আল্লাহ ছাড়া কোনো ইলাহ নেই, তিনি একক, তাঁর কোনো শরীক নেই; এবং মুহাম্মাদ (সা.) তাঁর বান্দা ও রাসূল। হে আল্লাহ! আমাকে তাওবাকারী ও পবিত্রদের অন্তর্ভুক্ত করুন।",
            "أشهد أن لا إله إلا الله وحده لا شريك له وأشهد أن محمداً عبده ورسوله. اللهم اجعلني من التوابين واجعلني من المتطهرين.")
    ),
    Dua(
        L("Entering the Mosque", "মসজিদে প্রবেশের দোয়া", "دخول المسجد"),
        "اللَّهُمَّ افْتَحْ لِي أَبْوَابَ رَحْمَتِكَ",
        L("Allahummaf-tah li abwaba rahmatik.", "আল্লাহুম্মাফতাহ লী আবওয়াবা রাহমাতিক।", "Allahummaf-tah li abwaba rahmatik."),
        L("O Allah, open for me the doors of Your mercy.", "হে আল্লাহ! আমার জন্য আপনার রহমতের দরজাগুলো খুলে দিন।", "اللهم افتح لي أبواب رحمتك.")
    ),
    Dua(
        L("Leaving the Mosque", "মসজিদ থেকে বের হওয়ার দোয়া", "الخروج من المسجد"),
        "اللَّهُمَّ إِنِّي أَسْأَلُكَ مِنْ فَضْلِكَ",
        L("Allahumma inni as'aluka min fadlik.", "আল্লাহুম্মা ইন্নী আসআলুকা মিন ফাদলিক।", "Allahumma inni as'aluka min fadlik."),
        L("O Allah, I ask You for Your bounty.", "হে আল্লাহ! আমি আপনার অনুগ্রহ প্রার্থনা করছি।", "اللهم إني أسألك من فضلك.")
    ),
    Dua(
        L("Thana (opening)", "ছানা", "دعاء الاستفتاح"),
        "سُبْحَانَكَ اللَّهُمَّ وَبِحَمْدِكَ وَتَبَارَكَ اسْمُكَ وَتَعَالَى جَدُّكَ وَلَا إِلَٰهَ غَيْرُكَ",
        L("Subhanakal-lahumma wa bihamdika, wa tabarakasmuka, wa ta'ala jadduka, wa la ilaha ghayruk.",
            "সুবহানাকাল্লাহুম্মা ওয়া বিহামদিকা, ওয়া তাবারাকাসমুকা, ওয়া তা'আলা জাদ্দুকা, ওয়া লা ইলাহা গাইরুক।",
            "Subhanakal-lahumma wa bihamdika, wa tabarakasmuka, wa ta'ala jadduka, wa la ilaha ghayruk."),
        L("Glory and praise to You, O Allah. Blessed is Your name, exalted is Your majesty, and there is no god but You.",
            "হে আল্লাহ! আপনি পবিত্র, সমস্ত প্রশংসা আপনার। আপনার নাম বরকতময়, আপনার মর্যাদা সমুন্নত এবং আপনি ছাড়া কোনো ইলাহ নেই।",
            "سبحانك اللهم وبحمدك، تبارك اسمك وتعالى جدك ولا إله غيرك.")
    ),
    Dua(
        L("Ta'awwudh & Tasmiyah", "তাআউয ও তাসমিয়া", "التعوذ والتسمية"),
        "أَعُوذُ بِاللَّهِ مِنَ الشَّيْطَانِ الرَّجِيمِ، بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ",
        L("A'udhu billahi minash-shaytanir-rajim. Bismillahir-Rahmanir-Rahim.",
            "আউযুবিল্লাহি মিনাশ শাইত্বানির রাজীম। বিসমিল্লাহির রাহমানির রাহীম।",
            "A'udhu billahi minash-shaytanir-rajim. Bismillahir-Rahmanir-Rahim."),
        L("I seek refuge in Allah from the accursed Satan. In the name of Allah, the Most Gracious, the Most Merciful.",
            "আমি বিতাড়িত শয়তান থেকে আল্লাহর আশ্রয় চাই। শুরু করছি পরম করুণাময় অতি দয়ালু আল্লাহর নামে।",
            "أعوذ بالله من الشيطان الرجيم. بسم الله الرحمن الرحيم.")
    ),
    Dua(
        L("In Ruku", "রুকুতে", "في الركوع"),
        "سُبْحَانَ رَبِّيَ الْعَظِيمِ",
        L("Subhana Rabbiyal-'Azim (3 times)", "সুবহানা রাব্বিয়াল আযীম (৩ বার)", "Subhana Rabbiyal-'Azim (3 times)"),
        L("Glory be to my Lord, the Magnificent.", "আমার মহান রবের পবিত্রতা ঘোষণা করছি।", "سبحان ربي العظيم.")
    ),
    Dua(
        L("Rising from Ruku", "রুকু থেকে ওঠার সময়", "الرفع من الركوع"),
        "سَمِعَ اللَّهُ لِمَنْ حَمِدَهُ، رَبَّنَا لَكَ الْحَمْدُ",
        L("Sami'allahu liman hamidah. Rabbana lakal-hamd.", "সামি'আল্লাহু লিমান হামিদাহ। রাব্বানা লাকাল হামদ।", "Sami'allahu liman hamidah. Rabbana lakal-hamd."),
        L("Allah hears the one who praises Him. Our Lord, all praise is Yours.", "যে আল্লাহর প্রশংসা করে আল্লাহ তা শোনেন। হে আমাদের রব! সমস্ত প্রশংসা আপনার।", "سمع الله لمن حمده، ربنا ولك الحمد.")
    ),
    Dua(
        L("In Sujood", "সিজদায়", "في السجود"),
        "سُبْحَانَ رَبِّيَ الْأَعْلَى",
        L("Subhana Rabbiyal-A'la (3 times)", "সুবহানা রাব্বিয়াল আ'লা (৩ বার)", "Subhana Rabbiyal-A'la (3 times)"),
        L("Glory be to my Lord, the Most High.", "আমার সর্বোচ্চ রবের পবিত্রতা ঘোষণা করছি।", "سبحان ربي الأعلى.")
    ),
    Dua(
        L("Between the two Sujood", "দুই সিজদার মাঝে", "بين السجدتين"),
        "رَبِّ اغْفِرْ لِي",
        L("Rabbighfir li.", "রাব্বিগফির লী।", "Rabbighfir li."),
        L("My Lord, forgive me.", "হে আমার রব! আমাকে ক্ষমা করুন।", "رب اغفر لي.")
    ),
    Dua(
        L("Tashahhud (At-Tahiyyat)", "তাশাহহুদ (আত্তাহিয়্যাতু)", "التشهد"),
        "التَّحِيَّاتُ لِلَّهِ وَالصَّلَوَاتُ وَالطَّيِّبَاتُ، السَّلَامُ عَلَيْكَ أَيُّهَا النَّبِيُّ وَرَحْمَةُ اللَّهِ وَبَرَكَاتُهُ، السَّلَامُ عَلَيْنَا وَعَلَى عِبَادِ اللَّهِ الصَّالِحِينَ، أَشْهَدُ أَنْ لَا إِلَٰهَ إِلَّا اللَّهُ وَأَشْهَدُ أَنَّ مُحَمَّدًا عَبْدُهُ وَرَسُولُهُ",
        L("At-tahiyyatu lillahi was-salawatu wat-tayyibat. As-salamu 'alayka ayyuhan-nabiyyu wa rahmatullahi wa barakatuh. As-salamu 'alayna wa 'ala 'ibadillahis-salihin. Ashhadu al-la ilaha illallah, wa ashhadu anna Muhammadan 'abduhu wa rasuluh.",
            "আত্তাহিয়্যাতু লিল্লাহি ওয়াস সালাওয়াতু ওয়াত তাইয়্যিবাত। আসসালামু আলাইকা আইয়্যুহান নাবিয়্যু ওয়া রাহমাতুল্লাহি ওয়া বারাকাতুহু। আসসালামু আলাইনা ওয়া আলা ইবাদিল্লাহিস সালিহীন। আশহাদু আল্লা ইলাহা ইল্লাল্লাহু ওয়া আশহাদু আন্না মুহাম্মাদান আবদুহু ওয়া রাসূলুহু।",
            "At-tahiyyatu lillahi was-salawatu wat-tayyibat. As-salamu 'alayka ayyuhan-nabiyyu wa rahmatullahi wa barakatuh. As-salamu 'alayna wa 'ala 'ibadillahis-salihin. Ashhadu al-la ilaha illallah, wa ashhadu anna Muhammadan 'abduhu wa rasuluh."),
        L("All greetings, prayers and good things are for Allah. Peace be upon you, O Prophet, and the mercy and blessings of Allah. Peace be upon us and upon the righteous servants of Allah. I bear witness that there is no god but Allah, and that Muhammad is His servant and messenger.",
            "সমস্ত সম্মান, ইবাদত ও পবিত্রতা আল্লাহর জন্য। হে নবী! আপনার ওপর শান্তি এবং আল্লাহর রহমত ও বরকত বর্ষিত হোক। আমাদের ওপর ও আল্লাহর নেককার বান্দাদের ওপর শান্তি বর্ষিত হোক। আমি সাক্ষ্য দিচ্ছি আল্লাহ ছাড়া কোনো ইলাহ নেই এবং মুহাম্মাদ (সা.) তাঁর বান্দা ও রাসূল।",
            "التحيات والصلوات والطيبات لله. السلام عليك أيها النبي ورحمة الله وبركاته، السلام علينا وعلى عباد الله الصالحين، أشهد أن لا إله إلا الله وأشهد أن محمداً عبده ورسوله.")
    ),
    Dua(
        L("Durood Ibrahim", "দরূদে ইবরাহীম", "الصلاة الإبراهيمية"),
        "اللَّهُمَّ صَلِّ عَلَى مُحَمَّدٍ وَعَلَى آلِ مُحَمَّدٍ كَمَا صَلَّيْتَ عَلَى إِبْرَاهِيمَ وَعَلَى آلِ إِبْرَاهِيمَ إِنَّكَ حَمِيدٌ مَجِيدٌ، اللَّهُمَّ بَارِكْ عَلَى مُحَمَّدٍ وَعَلَى آلِ مُحَمَّدٍ كَمَا بَارَكْتَ عَلَى إِبْرَاهِيمَ وَعَلَى آلِ إِبْرَاهِيمَ إِنَّكَ حَمِيدٌ مَجِيدٌ",
        L("Allahumma salli 'ala Muhammadin wa 'ala ali Muhammad, kama sallayta 'ala Ibrahima wa 'ala ali Ibrahim, innaka Hamidun Majid. Allahumma barik 'ala Muhammadin wa 'ala ali Muhammad, kama barakta 'ala Ibrahima wa 'ala ali Ibrahim, innaka Hamidun Majid.",
            "আল্লাহুম্মা সাল্লি আলা মুহাম্মাদিন ওয়া আলা আলি মুহাম্মাদ, কামা সাল্লাইতা আলা ইবরাহীমা ওয়া আলা আলি ইবরাহীম, ইন্নাকা হামীদুম মাজীদ। আল্লাহুম্মা বারিক আলা মুহাম্মাদিন ওয়া আলা আলি মুহাম্মাদ, কামা বারাকতা আলা ইবরাহীমা ওয়া আলা আলি ইবরাহীম, ইন্নাকা হামীদুম মাজীদ।",
            "Allahumma salli 'ala Muhammadin wa 'ala ali Muhammad, kama sallayta 'ala Ibrahima wa 'ala ali Ibrahim, innaka Hamidun Majid. Allahumma barik 'ala Muhammadin wa 'ala ali Muhammad, kama barakta 'ala Ibrahima wa 'ala ali Ibrahim, innaka Hamidun Majid."),
        L("O Allah, send prayers upon Muhammad and the family of Muhammad as You sent prayers upon Ibrahim and the family of Ibrahim; You are Praiseworthy and Glorious. O Allah, bless Muhammad and the family of Muhammad as You blessed Ibrahim and the family of Ibrahim; You are Praiseworthy and Glorious.",
            "হে আল্লাহ! মুহাম্মাদ (সা.) ও তাঁর পরিবারের ওপর রহমত বর্ষণ করুন, যেমন ইবরাহীম (আ.) ও তাঁর পরিবারের ওপর করেছেন; নিশ্চয়ই আপনি প্রশংসিত ও মহিমান্বিত। হে আল্লাহ! মুহাম্মাদ (সা.) ও তাঁর পরিবারের ওপর বরকত দান করুন, যেমন ইবরাহীম (আ.) ও তাঁর পরিবারের ওপর করেছেন; নিশ্চয়ই আপনি প্রশংসিত ও মহিমান্বিত।",
            "اللهم صل على محمد وعلى آل محمد كما صليت على إبراهيم وعلى آل إبراهيم إنك حميد مجيد، اللهم بارك على محمد وعلى آل محمد كما باركت على إبراهيم وعلى آل إبراهيم إنك حميد مجيد.")
    ),
    Dua(
        L("Dua before Salam", "সালামের আগের দোয়া", "دعاء قبل السلام"),
        "اللَّهُمَّ إِنِّي ظَلَمْتُ نَفْسِي ظُلْمًا كَثِيرًا وَلَا يَغْفِرُ الذُّنُوبَ إِلَّا أَنْتَ فَاغْفِرْ لِي مَغْفِرَةً مِنْ عِنْدِكَ وَارْحَمْنِي إِنَّكَ أَنْتَ الْغَفُورُ الرَّحِيمُ",
        L("Allahumma inni zalamtu nafsi zulman kathiran wa la yaghfirudh-dhunuba illa anta, faghfir li maghfiratam min 'indika warhamni, innaka antal-Ghafurur-Rahim.",
            "আল্লাহুম্মা ইন্নী যালামতু নাফসী যুলমান কাসীরান ওয়া লা ইয়াগফিরুয যুনূবা ইল্লা আনতা, ফাগফির লী মাগফিরাতাম মিন ইনদিকা ওয়ারহামনী, ইন্নাকা আনতাল গাফূরুর রাহীম।",
            "Allahumma inni zalamtu nafsi zulman kathiran wa la yaghfirudh-dhunuba illa anta, faghfir li maghfiratam min 'indika warhamni, innaka antal-Ghafurur-Rahim."),
        L("O Allah, I have greatly wronged myself and none forgives sins but You. So forgive me with forgiveness from You and have mercy on me. You are the Forgiving, the Merciful.",
            "হে আল্লাহ! আমি নিজের ওপর অনেক জুলুম করেছি এবং আপনি ছাড়া কেউ গুনাহ ক্ষমা করে না। তাই আপনার পক্ষ থেকে আমাকে ক্ষমা করুন ও দয়া করুন; নিশ্চয়ই আপনি ক্ষমাশীল, পরম দয়ালু।",
            "اللهم إني ظلمت نفسي ظلماً كثيراً ولا يغفر الذنوب إلا أنت، فاغفر لي مغفرة من عندك وارحمني إنك أنت الغفور الرحيم.")
    ),
    Dua(
        L("Dua Masura", "দোয়ায়ে মাসূরা (রাব্বানা আতিনা)", "ربنا آتنا"),
        "رَبَّنَا آتِنَا فِي الدُّنْيَا حَسَنَةً وَفِي الْآخِرَةِ حَسَنَةً وَقِنَا عَذَابَ النَّارِ",
        L("Rabbana atina fid-dunya hasanatan wa fil-akhirati hasanatan wa qina 'adhaban-nar.",
            "রাব্বানা আতিনা ফিদ দুনিয়া হাসানাতাঁও ওয়া ফিল আখিরাতি হাসানাতাঁও ওয়া ক্বিনা আযাবান নার।",
            "Rabbana atina fid-dunya hasanatan wa fil-akhirati hasanatan wa qina 'adhaban-nar."),
        L("Our Lord, give us good in this world and good in the Hereafter, and protect us from the punishment of the Fire. (Quran 2:201)",
            "হে আমাদের রব! আমাদের দুনিয়াতে কল্যাণ দিন, আখিরাতে কল্যাণ দিন এবং আমাদের জাহান্নামের আযাব থেকে রক্ষা করুন। (সূরা বাকারা ২:২০১)",
            "ربنا آتنا في الدنيا حسنة وفي الآخرة حسنة وقنا عذاب النار. (البقرة ٢٠١)")
    ),
    Dua(
        L("Dua Qunoot (Witr)", "দোয়া কুনূত (বিতর)", "دعاء القنوت"),
        "اللَّهُمَّ إِنَّا نَسْتَعِينُكَ وَنَسْتَغْفِرُكَ وَنُؤْمِنُ بِكَ وَنَتَوَكَّلُ عَلَيْكَ وَنُثْنِي عَلَيْكَ الْخَيْرَ وَنَشْكُرُكَ وَلَا نَكْفُرُكَ وَنَخْلَعُ وَنَتْرُكُ مَنْ يَفْجُرُكَ، اللَّهُمَّ إِيَّاكَ نَعْبُدُ وَلَكَ نُصَلِّي وَنَسْجُدُ وَإِلَيْكَ نَسْعَى وَنَحْفِدُ وَنَرْجُو رَحْمَتَكَ وَنَخْشَى عَذَابَكَ إِنَّ عَذَابَكَ بِالْكُفَّارِ مُلْحِقٌ",
        L("Allahumma inna nasta'inuka wa nastaghfiruka wa nu'minu bika wa natawakkalu 'alayka wa nuthni 'alaykal-khayr, wa nashkuruka wa la nakfuruka, wa nakhla'u wa natruku man yafjuruk. Allahumma iyyaka na'budu wa laka nusalli wa nasjudu wa ilayka nas'a wa nahfid, wa narju rahmataka wa nakhsha 'adhabaka inna 'adhabaka bil-kuffari mulhiq.",
            "আল্লাহুম্মা ইন্না নাস্তাঈনুকা ওয়া নাস্তাগফিরুকা ওয়া নুমিনু বিকা ওয়া নাতাওয়াক্কালু আলাইকা ওয়া নুসনী আলাইকাল খাইর, ওয়া নাশকুরুকা ওয়া লা নাকফুরুকা, ওয়া নাখলাউ ওয়া নাতরুকু মাই ইয়াফজুরুক। আল্লাহুম্মা ইইয়াকা নাবুদু ওয়া লাকা নুসাল্লী ওয়া নাসজুদু ওয়া ইলাইকা নাসআ ওয়া নাহফিদু, ওয়া নারজূ রাহমাতাকা ওয়া নাখশা আযাবাকা ইন্না আযাবাকা বিল কুফফারি মুলহিক।",
            "Allahumma inna nasta'inuka wa nastaghfiruka wa nu'minu bika wa natawakkalu 'alayka wa nuthni 'alaykal-khayr, wa nashkuruka wa la nakfuruka, wa nakhla'u wa natruku man yafjuruk. Allahumma iyyaka na'budu wa laka nusalli wa nasjudu wa ilayka nas'a wa nahfid, wa narju rahmataka wa nakhsha 'adhabaka inna 'adhabaka bil-kuffari mulhiq."),
        L("O Allah, we seek Your help and forgiveness, believe in You, rely on You and praise You with all good. We thank You and are not ungrateful, and we abandon whoever disobeys You. O Allah, You alone we worship; for You we pray and prostrate; to You we strive; we hope for Your mercy and fear Your punishment, which will surely reach the disbelievers.",
            "হে আল্লাহ! আমরা আপনার কাছে সাহায্য ও ক্ষমা চাই, আপনার প্রতি ঈমান আনি, আপনার ওপর ভরসা করি এবং উত্তমরূপে আপনার প্রশংসা করি। আমরা আপনার শুকরিয়া আদায় করি, অকৃতজ্ঞ হই না এবং যারা আপনার অবাধ্য তাদের ত্যাগ করি। হে আল্লাহ! আমরা শুধু আপনারই ইবাদত করি, আপনার জন্যই নামাজ পড়ি ও সিজদা করি, আপনার দিকেই ধাবিত হই, আপনার রহমতের আশা করি ও আপনার আযাবকে ভয় করি; নিশ্চয়ই আপনার আযাব কাফিরদের ওপর পতিত হবে।",
            "اللهم إنا نستعينك ونستغفرك ونؤمن بك ونتوكل عليك ونثني عليك الخير ونشكرك ولا نكفرك ونخلع ونترك من يفجرك. اللهم إياك نعبد ولك نصلي ونسجد وإليك نسعى ونحفد ونرجو رحمتك ونخشى عذابك إن عذابك بالكفار ملحق.")
    ),
    Dua(
        L("After Salah: Dhikr", "নামাজের পর যিকির", "ذكر بعد الصلاة"),
        "أَسْتَغْفِرُ اللَّهَ (٣) اللَّهُمَّ أَنْتَ السَّلَامُ وَمِنْكَ السَّلَامُ تَبَارَكْتَ يَا ذَا الْجَلَالِ وَالْإِكْرَامِ",
        L("Astaghfirullah (3 times). Allahumma antas-Salam wa minkas-salam, tabarakta ya Dhal-Jalali wal-Ikram. Then SubhanAllah 33, Alhamdulillah 33, Allahu Akbar 34.",
            "আস্তাগফিরুল্লাহ (৩ বার)। আল্লাহুম্মা আনতাস সালাম ওয়া মিনকাস সালাম, তাবারাকতা ইয়া যাল জালালি ওয়াল ইকরাম। তারপর সুবহানাল্লাহ ৩৩, আলহামদুলিল্লাহ ৩৩, আল্লাহু আকবার ৩৪ বার।",
            "Astaghfirullah (3 times). Allahumma antas-Salam wa minkas-salam, tabarakta ya Dhal-Jalali wal-Ikram. Then SubhanAllah 33, Alhamdulillah 33, Allahu Akbar 34."),
        L("I seek Allah's forgiveness. O Allah, You are Peace and from You is peace; blessed are You, O Possessor of Majesty and Honour.",
            "আমি আল্লাহর কাছে ক্ষমা চাই। হে আল্লাহ! আপনি শান্তিময়, আপনার কাছ থেকেই শান্তি; হে মহিমা ও সম্মানের অধিকারী, আপনি বরকতময়।",
            "أستغفر الله. اللهم أنت السلام ومنك السلام، تباركت يا ذا الجلال والإكرام.")
    ),
)

val DUA_SEHRI = Dua(
    L("Sehri intention", "সেহরির নিয়ত", "نية السحور"),
    "وَبِصَوْمِ غَدٍ نَّوَيْتُ مِنْ شَهْرِ رَمَضَانَ",
    L("Wa bisawmi ghadin nawaytu min shahri Ramadan.", "ওয়া বিসাউমি গাদিন নাওয়াইতু মিন শাহরি রামাদান।", "Wa bisawmi ghadin nawaytu min shahri Ramadan."),
    L("I intend to keep the fast tomorrow in the month of Ramadan.", "আমি রমজান মাসের আগামীকালের রোজা রাখার নিয়ত করলাম।", "نويت صوم غدٍ من شهر رمضان.")
)

val DUA_IFTAR = Dua(
    L("Iftar dua", "ইফতারের দোয়া", "دعاء الإفطار"),
    "ذَهَبَ الظَّمَأُ وَابْتَلَّتِ الْعُرُوقُ وَثَبَتَ الْأَجْرُ إِنْ شَاءَ اللَّهُ",
    L("Dhahaba az-zama'u wabtallatil-'uruqu wa thabatal-ajru insha Allah. (Also commonly said: Allahumma laka sumtu wa 'ala rizqika aftartu.)",
        "যাহাবায যামাউ ওয়াবতাল্লাতিল উরূকু ওয়া সাবাতাল আজরু ইনশাআল্লাহ। (প্রচলিত: আল্লাহুম্মা লাকা সুমতু ওয়া আলা রিযক্বিকা আফতারতু।)",
        "Dhahaba az-zama'u wabtallatil-'uruqu wa thabatal-ajru insha Allah. (Also commonly said: Allahumma laka sumtu wa 'ala rizqika aftartu.)"),
    L("The thirst has gone, the veins are moistened, and the reward is confirmed, if Allah wills.",
        "পিপাসা দূর হয়েছে, শিরা-উপশিরা সিক্ত হয়েছে এবং ইনশাআল্লাহ প্রতিদান নিশ্চিত হয়েছে।",
        "ذهب الظمأ وابتلت العروق وثبت الأجر إن شاء الله.")
)
