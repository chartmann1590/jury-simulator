// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    // AGP 8.5.1+ required for 16 KB page-size zip alignment of uncompressed native libs (Play / devices with 16 KB pages).
    id("com.android.application") version "8.5.2" apply false
    id("org.jetbrains.kotlin.android") version "1.9.20" apply false
    id("com.google.devtools.ksp") version "1.9.20-1.0.14" apply false
}
