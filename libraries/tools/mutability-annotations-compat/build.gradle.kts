description = "Compatibility artifact with Mutable and ReadOnly annotations"

plugins {
    java
    id("jps-compatible")
}

jvmTarget = "1.8"

tasks.withType<JavaCompile> {
    sourceCompatibility = "1.8"
    targetCompatibility = "1.8"
}

sourceSets {
    "main" { projectDefault() }
    "test" {}
}

dist()
