# RosaFiesta Admin Android Release R8 / ProGuard rules
-dontwarn java.lang.invoke.StringConcatFactory
-dontwarn org.slf4j.**
-dontwarn com.google.firebase.**
-dontwarn com.google.android.gms.**

# Kotlin Serialization & Ktor DTOs
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod
-keepclassmembers class * {
    @kotlinx.serialization.Serializable <fields>;
}
-keep,includedescriptorclasses class com.jeudry.rosafiesta.data.**$$serializer { *; }
-keepclassmembers class com.jeudry.rosafiesta.data.** {
    *** Companion;
}

# Android entrypoints & native bridges
-keep public class com.jeudry.rosafiesta.admin.android.AdminMainActivity { *; }
-keepclassmembers class com.jeudry.rosafiesta.data.** {
    *;
}
