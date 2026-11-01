# Sehhaty Production ProGuard Rules
# MTA TECHNOLOGIES

# Retain Room Database, generated implementations & DAOs
-keep class com.mtaapps.sehhaty.data.local.** { *; }
-keep class * extends androidx.room.RoomDatabase {
    <init>();
    *;
}
-keep class * extends androidx.room.RoomDatabase
-keep class **_Impl {
    public <init>();
    public <init>(...);
    *;
}
-keepclassmembers class * extends androidx.room.RoomDatabase {
    void <init>();
    *;
}
-keepclassmembers class com.mtaapps.sehhaty.data.local.SehhatyDatabase {
    *;
}
-keepclassmembers class com.mtaapps.sehhaty.data.local.SehhatyDatabase_Impl {
    public <init>();
    *;
}
-keep class com.mtaapps.sehhaty.data.local.entity.** { *; }
-keep interface com.mtaapps.sehhaty.data.local.dao.** { *; }
-dontwarn androidx.room.paging.**

# Keep SQLite and Room reflection
-keep class androidx.room.** { *; }
-keep class androidx.sqlite.** { *; }

# Firebase
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod

# AdMob
-keep public class com.google.android.gms.ads.** { public *; }
-keep public class com.google.ads.** { public *; }

# Keep models & domain classes
-keep class com.mtaapps.sehhaty.data.model.** { *; }
