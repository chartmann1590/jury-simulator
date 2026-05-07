# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.kts.

# Keep model data classes (still used in-memory by SimulationViewModel etc.).
-keep class com.jurysim.data.model.** { *; }

# LiteRT-LM ships with a Java/Kotlin facade that delegates to native code.
# Keep its public API surface untouched so reflection / JNI bindings work.
-keep class com.google.ai.edge.litertlm.** { *; }
-keep class com.google.ai.edge.litert.** { *; }
