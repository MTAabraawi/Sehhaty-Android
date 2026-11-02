=======================================================
حزمة الملفات المصححة فقط لمشروع Sehhaty - صحتي
فرع العمل: sehhaty-fixed-test | Package ID: com.mtaapps.sehhaty
=======================================================

هذه الحزمة تحتوي فقط وحصريًا على الملفات التي تم تعديلها لإصلاح أخطاء البناء في GitHub Actions.

محتويات الحزمة:
1. app/build.gradle.kts
   - تصحيح libs.play.services.auth و libs.play.services.ads (بدل الشرطات التي كانت تسبب خطأ Unresolved reference).

2. app/src/main/AndroidManifest.xml
   - إضافة tools:replace="android:resource" لوسم AD_SERVICES_CONFIG لحل تعارض الدمج بين Firebase و AdMob.

3. app/src/main/res/values/strings.xml
   - تهريب الفاصلة العليا في السطر 13 (Today's Doses) لمنع خطأ AAPT2 Failed to flatten XML.

4. app/src/main/res/drawable/ic_launcher_foreground.xml
   - ملف الفيكتور التكيفي للأيقونة.
   * ملاحظة حاسمة: يجب حذف ملف 'app/src/main/res/drawable/ic_launcher_foreground.png' من المستودع لتجنب خطأ AAPT2 Duplicate resources.

5. app/src/main/java/com/mtaapps/sehhaty/alarm/BootReceiver.kt
   - تصحيح Intent.ACTION_TIME_CHANGED بدلاً من Intent.ACTION_TIME_SET غير المعرف.

6. app/src/main/java/com/mtaapps/sehhaty/data/repository/DrugRepository.kt
   - كشف دالة getAllDrugs() لتمريرها من DAO وحل خطأ استنتاج النوع في Compose.

7. app/src/main/java/com/mtaapps/sehhaty/ui/theme/Color.kt
   - إضافة متغيرات التوافق الخلفي (TealPrimary, TextSecondary, CardSurface).

8. .github/workflows/android-build.yml
   - تفعيل تشغيل Workflow تلقائيًا عند الدفع لفرع sehhaty-fixed-test.

-------------------------------------------------------
طريقة التطبيق السريعة عبر Git:
1. انسخ هذه الملفات بنفس مساراتها فوق مجلد مشروعك المحلي على فرع 'sehhaty-fixed-test'.
2. احذف الملف المكرر:
   git rm app/src/main/res/drawable/ic_launcher_foreground.png
3. نفّذ الـ commit والدفع:
   git add .
   git commit -m "Fix: apply targeted build fixes for GitHub Actions"
   git push origin sehhaty-fixed-test
-------------------------------------------------------
