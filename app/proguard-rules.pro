-keepattributes Signature
-keepattributes *Annotation*

-keep class com.abuidris.reports.data.local.** { *; }
-keep class com.abuidris.reports.data.remote.** { *; }

-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**

-keep class com.google.firebase.** { *; }
-dontwarn com.google.firebase.**
