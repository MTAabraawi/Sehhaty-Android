package com.mtaapps.sehhaty.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.mtaapps.sehhaty.data.local.dao.*
import com.mtaapps.sehhaty.data.local.entity.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Sehhaty Room Database.
 * - Thread-safe singleton with double-checked locking.
 * - Safe migration: NO destructive migrations so patient records are never wiped.
 * - Robust data seeding: Uses database instance directly after build to avoid null INSTANCE race conditions.
 */
@Database(
    entities = [
        MedicationEntity::class,
        DoseScheduleEntity::class,
        DoseLogEntity::class,
        SupervisorEntity::class,
        DrugReferenceEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class SehhatyDatabase : RoomDatabase() {

    abstract fun medicationDao(): MedicationDao
    abstract fun doseDao(): DoseDao
    abstract fun supervisorDao(): SupervisorDao
    abstract fun drugReferenceDao(): DrugReferenceDao

    companion object {
        @Volatile
        private var INSTANCE: SehhatyDatabase? = null

        // Explicit migration to preserve user data across future schema updates
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Safe migration placeholder for schema enhancements
            }
        }

        fun getDatabase(context: Context): SehhatyDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(context).also { INSTANCE = it }
            }
        }

        private fun buildDatabase(context: Context): SehhatyDatabase {
            val db = Room.databaseBuilder(
                context.applicationContext,
                SehhatyDatabase::class.java,
                "sehhaty_database"
            )
            .addMigrations(MIGRATION_1_2)
            // Strictly NO fallbackToDestructiveMigration
            .build()

            // Seed formulary safely without depending on INSTANCE variable
            CoroutineScope(Dispatchers.IO).launch {
                seedDatabaseIfNeeded(db)
            }

            return db
        }

        private suspend fun seedDatabaseIfNeeded(database: SehhatyDatabase) {
            try {
                if (database.drugReferenceDao().countDrugs() == 0) {
                    val initialDrugs = listOf(
                        DrugReferenceEntity(
                            tradeNameAr = "بنادول إكسترا",
                            tradeNameEn = "Panadol Extra",
                            scientificName = "Paracetamol + Caffeine",
                            activeIngredient = "باراسيتامول 500 ملغ + كافيين 65 ملغ",
                            strength = "500mg / 65mg",
                            dosageForm = "أقراص مغلفة",
                            approvedIndications = "مسكن للآلام الخفيفة إلى المتوسطة مثل الصداع، آلام الأسنان، آلام العضلات، وخافض للحرارة.",
                            contraindications = "فرط الحساسية للباراسيتامول أو الكافيين، مرضى القصور الكبدي الشديد.",
                            commonSideEffects = "نادر الحدوث عند الجرعات الموصى بها: أرق بسبب الكافيين، اضطراب معدة خفيف.",
                            drugInteractions = "أدوية السيولة مثل الوارفارين عند الاستخدام المطول، مضادات الاكتئاب التي تحتوي على مثبطات MAOI.",
                            usageInstructions = "قرص أو قرصان كل 4 إلى 6 ساعات حسب الحاجة. الحد الأقصى 8 أقراص خلال 24 ساعة. لا تتناول أدوية أخرى تحتوي على باراسيتامول.",
                            manufacturer = "GlaxoSmithKline (GSK)",
                            medicalSource = "هيئة الغذاء والدواء السعودية (SFDA) والنشرة الرسمية",
                            lastUpdateDate = "2025-01-10"
                        ),
                        DrugReferenceEntity(
                            tradeNameAr = "أوجمنتين",
                            tradeNameEn = "Augmentin",
                            scientificName = "Amoxicillin + Clavulanic Acid",
                            activeIngredient = "أموكسيسيلين + حمض الكلافولانيك",
                            strength = "1g (875mg/125mg)",
                            dosageForm = "أقراص مغلفة",
                            approvedIndications = "علاج الالتهابات البكتيرية في الجهاز التنفسي، التهاب الأذن الوسطى، والتهابات المسالك البولية والجلد.",
                            contraindications = "حساسية البنسلين أو السيفالوسبورين، وتاريخ من اليرقان الكبدي المرتبط بالبنسلين.",
                            commonSideEffects = "إسهال، غثيان، طفح جلدي خفيف، تقلصات بطن.",
                            drugInteractions = "ألوبيورينول، حبوب منع الحمل الفموية (قد تقل فعاليتها)، الميثوتريكسات.",
                            usageInstructions = "قرص واحد كل 12 ساعة مع بداية وجبة طعام لتقليل اضطرابات الجهاز الهضمي. يجب إكمال كامل الكورس العلاجي المحدد.",
                            manufacturer = "GSK Pharmaceuticals",
                            medicalSource = "الدليل الصيدلاني البريطاني BNF والنشرة الدوائية المعتمدة",
                            lastUpdateDate = "2025-02-01"
                        ),
                        DrugReferenceEntity(
                            tradeNameAr = "كونكور",
                            tradeNameEn = "Concor",
                            scientificName = "Bisoprolol Fumarate",
                            activeIngredient = "بيسوبرولول فومارات",
                            strength = "5 mg",
                            dosageForm = "أقراص",
                            approvedIndications = "ارتفاع ضغط الدم، والذبحة الصدرية، والقصور القلبي المزمن المستقر.",
                            contraindications = "الصدمة القلبية، بطء ضربات القلب الشديد، الربو الشعبي الحاد، انخفاض ضغط الدم الحاد.",
                            commonSideEffects = "شعور بالإرهاق، برودة الأطراف، دوخة، بطء النبض.",
                            drugInteractions = "حاصرات قنوات الكالسيوم مثل فيراباميل وديلتيازيم، ديجوكسين، أدوية السكري.",
                            usageInstructions = "قرص واحد صباحاً مع أو بدون طعام. لا توقف تناول الدواء فجأة دون استشارة الطبيب لتجنب ارتداد الضغط.",
                            manufacturer = "Merck KGaA",
                            medicalSource = "الجمعية الأوروبية لأمراض القلب (ESC)",
                            lastUpdateDate = "2025-01-20"
                        ),
                        DrugReferenceEntity(
                            tradeNameAr = "جلوكوفاج",
                            tradeNameEn = "Glucophage",
                            scientificName = "Metformin Hydrochloride",
                            activeIngredient = "ميتفورمين هيدروكلوريد",
                            strength = "500 mg / 1000 mg",
                            dosageForm = "أقراص ممتدة المفعول (XR)",
                            approvedIndications = "علاج داء السكري من النوع الثاني لتحسين استجابة الجسم للإنسولين.",
                            contraindications = "الحماض الكيتوني السكري، القصور الكلوي الشديد (eGFR < 30)، الفشل القلبي الحاد.",
                            commonSideEffects = "اضطرابات معوية، غازات، إسهال مؤقت، طعم معدني في الفم.",
                            drugInteractions = "الصبغات الإشعاعية الوريدية التي تحتوي على اليود، مدرات البول.",
                            usageInstructions = "يؤخذ مع الوجبة المسائية أو بعدها مباشرة لتقليل الأعراض الهضمية. يُبلع كاملاً دون سحق.",
                            manufacturer = "Merck Santé",
                            medicalSource = "الجمعية الأمريكية للسكري (ADA)",
                            lastUpdateDate = "2025-02-15"
                        ),
                        DrugReferenceEntity(
                            tradeNameAr = "نيكسيوم",
                            tradeNameEn = "Nexium",
                            scientificName = "Esomeprazole Magnesium",
                            activeIngredient = "إيزوميبرازول",
                            strength = "20 mg / 40 mg",
                            dosageForm = "أقراص مقاومة لإفرازات المعدة",
                            approvedIndications = "ارتجاع المريء المعدي، قرحة المعدة والاثني عشر، الوقاية من تقرحات المسكنات.",
                            contraindications = "فرط الحساسية لمثبطات مضخة البروتون، الاستخدام المتزامن مع ريلبيفيرين.",
                            commonSideEffects = "صداع، إمساك أو إسهال، آلام في البطن.",
                            drugInteractions = "كلوبيدوجريل (بلافيكس)، مضادات الفطريات (كيتوكونازول)، مكملات الحديد.",
                            usageInstructions = "قرص واحد يومياً في الصباح قبل الإفطار بنصف ساعة إلى ساعة مع كوب ماء كامل.",
                            manufacturer = "AstraZeneca",
                            medicalSource = "نشرة إدارة الغذاء والدواء الأمريكية FDA",
                            lastUpdateDate = "2025-01-30"
                        )
                    )
                    database.drugReferenceDao().insertDrugs(initialDrugs)
                }
            } catch (e: Exception) {
                android.util.Log.e("SehhatyDatabase", "Error seeding drug formulary", e)
            }
        }
    }
}
