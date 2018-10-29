
description = "Kotlin Gradle Tooling support"

plugins {
    kotlin("jvm")
    id("jps-compatible")
}

jvmTarget = "1.8"

dependencies {
    compile(project(":kotlin-stdlib"))
    compile(project(":compiler:cli-common"))
    compile(intellijPluginDep("gradle"))
    compileOnly(intellijDep()) { includeJars("slf4j-api-1.7.25") }
}

sourceSets {
    "main" { projectDefault() }
    "test" {}
}

runtimeJar()

ideaPlugin()
