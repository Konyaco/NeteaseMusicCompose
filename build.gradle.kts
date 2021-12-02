import org.jetbrains.compose.compose
import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("jvm") version "1.5.31"
    id("org.jetbrains.compose") version "1.0.0-rc6"
    id("org.bytedeco.gradle-javacpp-platform") version "1.5.6"
}

group = "me.konyaco.neteasemusic"
version = "1.0.2"

repositories {
    google()
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
}

dependencies {
    implementation(compose.desktop.currentOs)
//    implementation("org.bytedeco:javacv:1.5.6")
    implementation("org.bytedeco:ffmpeg-platform:4.4-1.5.6")
    implementation(files("libs/javacv.jar"))
    testImplementation(kotlin("test-junit5"))
}

tasks {
    test {
        useJUnitPlatform()
    }
    compileKotlin {
        kotlinOptions.jvmTarget = "11"
    }
}

compose.desktop {
    application {
        mainClass = "me.konyaco.neteasemusic.MainKt"
        jvmArgs("-Dfile.encoding=UTF-8")
        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "NeteaseMusic"
            packageVersion = "1.0.2"
            vendor = "Konyaco"
            windows {
                perUserInstall = true
                shortcut = true
                upgradeUuid = "4360cdad-100a-42a3-9d44-e7f4bdf3a3b9"
                menu = true
                menuGroup = "Konyaco"
            }
            linux {
                shortcut = true
                menuGroup = "Konyaco"
            }
        }
    }
}