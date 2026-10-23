# kotlinx-serialization : la bibliothèque embarque ses propres règles R8 ; on garde en plus
# les serializers générés des classes du catalogue (lues depuis les assets JSON).
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.**
-keepclassmembers @kotlinx.serialization.Serializable class io.github.achmalearning.externat.** {
    *** Companion;
    kotlinx.serialization.KSerializer serializer(...);
}
-keepclasseswithmembers class io.github.achmalearning.externat.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class io.github.achmalearning.externat.**$$serializer { *; }
