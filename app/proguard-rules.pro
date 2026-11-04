# Sehhaty Production ProGuard Rules
# MTA TECHNOLOGIES

# Retain Room Database entities & DAOs
-keep class com.mtaapps.sehhaty.data.local.entity.** { *; }
-keep interface com.mtaapps.sehhaty.data.local.dao.** { *; }

# Firebase
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod

# AdMob
-keep public class com.google.android.gms.ads.** { public *; }
-keep public class com.google.ads.** { public *; }

# Keep models & domain classes
-keep class com.mtaapps.sehhaty.data.model.** { *; }

# Room Database: Preserve RoomDatabase and generated _Impl classes
-keep class * extends androidx.room.RoomDatabase {
    <init>();
}
-keep class com.mtaapps.sehhaty.data.local.** { *; }
-keepclassmembers class * extends androidx.room.RoomDatabase {
    *;
}
-dontwarn androidx.room.paging.**
