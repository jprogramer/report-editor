import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("multiplatform")
    id("org.jetbrains.compose")
}


val buildVersion = "0.1.0"
version = buildVersion
group = "dz.nexatech.reporter"

repositories {
    google()
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
}

kotlin {
    jvm {
        compilations.all {
            kotlinOptions.jvmTarget = "17"
        }
        withJava()
    }
    sourceSets {
        val jvmMain by getting {
            dependencies {
                implementation(compose.desktop.currentOs)
            }
        }
        val jvmTest by getting
    }
}

compose.desktop {
    application {
        mainClass = "MainKt"
        nativeDistributions {
            targetFormats(TargetFormat.Msi)
            packageName = "editor"
            packageVersion = buildVersion
        }
    }
}

repositories {
    mavenLocal()
    google()
    mavenCentral()
}

dependencies {
    add("implementation", "dz.nexatech:reporter-core:+")
    add("implementation", "org.jetbrains.kotlin:kotlin-stdlib-jdk8:" + project.property("kotlin.version"))
    add("implementation", "com.google.guava:guava:31.1-jre")
    add("implementation", "org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")

    add("implementation", files("libs/typography-3.0.2.jar"))
    add("implementation", "io.pebbletemplates:pebble:3.2.0")
    add("implementation", "com.itextpdf:itext7-core:7.2.5")
    add("implementation", "com.itextpdf:html2pdf:4.0.5")

    add("implementation", "ch.qos.logback:logback-classic:1.4.7")
    add("implementation", "com.google.code.gson:gson:2.10.1")

    add("testImplementation", "junit:junit:4.13.2")
    add("testImplementation", "com.google.truth:truth:1.1.3")
}