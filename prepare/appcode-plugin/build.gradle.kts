import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import com.github.jk1.tcdeps.KotlinScriptDslAdapter.teamcityServer
import com.github.jk1.tcdeps.KotlinScriptDslAdapter.tc
import org.gradle.kotlin.dsl.support.zipTo
import org.jetbrains.kotlin.cidr.includePatchedJavaXmls

apply {
    plugin("kotlin")
}

plugins {
    id("com.github.jk1.tcdeps") version "0.17"
}

repositories {
    teamcityServer {
        setUrl("http://buildserver.labs.intellij.net")
        credentials {
            username = "guest"
            password = "guest"
        }
    }
}

val cidrPluginDir: File by rootProject.extra
val appcodePluginDir: File by rootProject.extra
val appcodeVersion = rootProject.extra["versions.appcode"] as String
val appcodeVersionRepo = rootProject.extra["versions.appcode.repo"] as String
val kotlinNativeVersion = rootProject.extra["versions.kotlin.native"] as String
val kotlinNativeRepo = rootProject.extra["versions.kotlin.native.repo"] as String

val cidrPlugin by configurations.creating
val platformDepsZip by configurations.creating
val sideJars by configurations.creating

val pluginXmlPath = "META-INF/plugin.xml"
val platformDepsJarName = "kotlinNative-platformDeps.jar"

// Do not rename, used in JPS importer
val projectsToShadow by extra(listOf(
    ":kotlin-ultimate:cidr-native",
    ":kotlin-ultimate:appcode-native"))


dependencies {
    cidrPlugin(project(":prepare:cidr-plugin"))
    platformDepsZip(tc("$appcodeVersionRepo:$appcodeVersion:OC-plugins/kotlinNative-platformDeps-$appcodeVersion.zip"))
    sideJars(tc("$kotlinNativeRepo:$kotlinNativeVersion:backend.native.jar"))
}

val kotlinPluginXml by tasks.creating {
    inputs.files(cidrPlugin)
    outputs.files(fileFrom(buildDir, name, "META-INF/KotlinPlugin.xml"))

    doFirst {
        val pluginXmlText = zipTree(inputs.files.singleFile)
            .matching { include(pluginXmlPath) }
            .singleFile
            .readText()
        outputs.files.singleFile.writeText(pluginXmlText)
    }
}

val jar = runtimeJar {
    archiveName = "kotlin-plugin.jar"
    dependsOn(cidrPlugin)
    from(kotlinPluginXml) { into("META-INF") }

    from {
        zipTree(cidrPlugin.singleFile).matching {
            exclude(pluginXmlPath)
        }
    }

    for (p in projectsToShadow) {
        dependsOn("$p:classes")
        from(getSourceSetsFrom(p)["main"].output)
    }
}

val platformDepsJar by task<Zip> {
    archiveName = platformDepsJarName
    val platformDepsJar = zipTree(platformDepsZip.singleFile).matching { include("**/$platformDepsJarName") }.singleFile
    from(zipTree(platformDepsJar)) {
        exclude(pluginXmlPath)
        includePatchedJavaXmls()
    }
}

task<Copy>("appcodePlugin") {
    into(appcodePluginDir)
    from(cidrPluginDir) { exclude("lib/kotlin-plugin.jar") }

    into("lib") {
        from(jar)
        from(sideJars)
        from(platformDepsJar)
        from(zipTree(platformDepsZip.singleFile).files) {
            exclude("**/$platformDepsJarName")
        }
    }
    into("templates") {
        from(File(project(":kotlin-ultimate:appcode-native").projectDir, "templates"))
    }
}
