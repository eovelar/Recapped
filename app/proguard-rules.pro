# Reglas Proguard básicas
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.recapped.app.data.remote.dto.** { *; }
-dontwarn okio.**
