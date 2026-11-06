# Stack traces & Kotlin metadata
# Preserve line numbers for readable crash reports while hiding local file paths
-keepattributes SourceFile,LineNumberTable,Signature,InnerClasses,EnclosingMethod,*Annotation*,ElementValueAttribute
-renamesourcefileattribute SourceFile


# ONNX Runtime (JNI reflection)
# Native C++ code (libonnxruntime4j_jni.so) looks up these classes and methods by exact name
-keep class ai.onnxruntime.** { *; }
-keep interface ai.onnxruntime.** { *; }
-dontwarn ai.onnxruntime.**


# WorkManager
# Keep Worker class names stable across app updates for persisted WorkManager jobs
-keep class * extends androidx.work.ListenableWorker {
    public <init>(...);
}


# Kotlinx Serialization (R8 Full Mode)
-dontnote kotlinx.serialization.AnnotationsKt

# Keep Companion objects only on classes annotated with @Serializable
-keepattributes RuntimeVisibleAnnotations,AnnotationDefault
-if @kotlinx.serialization.Serializable class **
-keepclassmembers class <1> {
    static <1>$Companion Companion;
}

# Keep serializer() methods on Companion objects of @Serializable classes
-if @kotlinx.serialization.Serializable class ** {
    static **$* *;
}
-keepclassmembers class <2>$<3> {
    kotlinx.serialization.KSerializer serializer(...);
}

# Keep @SerialName fields and generated $$serializer classes
-keepclassmembers class * {
    @kotlinx.serialization.SerialName <fields>;
}
-keep,includedescriptorclasses class es.pile.**$$serializer { *; }