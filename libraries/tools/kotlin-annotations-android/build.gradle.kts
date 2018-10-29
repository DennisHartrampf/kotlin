import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

description = "Kotlin annotations for Android"

plugins {
    kotlin("jvm")
}

jvmTarget = "1.8"
javaHome = rootProject.extra["JDK_18"] as String

tasks.withType<KotlinCompile> {
    kotlinOptions.freeCompilerArgs += listOf(
            "-Xallow-kotlin-package",
            "-module-name", project.name
    )
}

sourceSets {
    "main" {
        projectDefault()
    }
}

sourcesJar()
javadocJar()
runtimeJar()
dist()

publish()
