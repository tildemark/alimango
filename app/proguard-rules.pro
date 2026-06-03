# Add project-specific Proguard rules here.
# By default, the noise in default rules is file-based, but you can
# customize them here.

# Keep Room generated code
-keep class * extends androidx.room.RoomDatabase
-keep class * implements androidx.room.RoomDatabase$Callback

# Keep Kotlinx Serialization
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod
-keepclassmembers class * {
    *** Companion;
}
-keepclassmembers class * {
    @org.jetbrains.kotlinx.serialization.Serializable *;
}
-keep class org.jetbrains.kotlinx.serialization.json.** { *; }

# Keep Ktor
-keep class io.ktor.** { *; }
