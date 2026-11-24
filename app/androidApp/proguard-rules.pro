# RosaFiesta Personal Android Release R8 / ProGuard rules
-dontwarn java.lang.invoke.StringConcatFactory
-dontwarn org.slf4j.**
-dontwarn com.google.firebase.**
-dontwarn com.google.android.gms.**

# Kotlin Serialization & Ktor DTOs
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod
-keep @kotlinx.serialization.Serializable class * { *; }
-keepclassmembers class * {
    @kotlinx.serialization.Serializable <fields>;
    public static *** serializer(...);
}
-keepclassmembers class **$Companion {
    public kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class com.jeudry.rosafiesta.data.**$$serializer { *; }
-keepclassmembers class com.jeudry.rosafiesta.data.** {
    *** Companion;
}

# FileProvider & Firebase Messaging reflection bridges
-keep class androidx.core.content.FileProvider {
    public static android.net.Uri getUriForFile(android.content.Context, java.lang.String, java.io.File);
}
-keep class com.google.firebase.messaging.FirebaseMessaging {
    public static com.google.firebase.messaging.FirebaseMessaging getInstance();
    public com.google.android.gms.tasks.Task getToken();
}
-keep interface com.google.android.gms.tasks.OnCompleteListener {
    public void onComplete(com.google.android.gms.tasks.Task);
}
-keep class com.google.android.gms.tasks.** {
    public boolean isSuccessful();
    public java.lang.Object getResult();
    public com.google.android.gms.tasks.Task addOnCompleteListener(com.google.android.gms.tasks.OnCompleteListener);
}

# Android entrypoints & native bridges
-keep public class com.jeudry.rosafiesta.android.MainActivity { *; }
-keepclassmembers class com.jeudry.rosafiesta.data.** {
    *;
}
