plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "dev.test.blurnav"
    compileSdk = 36

    defaultConfig {
        applicationId = "dev.test.blurnav"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
    }
    buildFeatures { compose = true }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    // Подпись релиза: ключ и пароли приходят из переменных окружения (GitHub Secrets).
    // Если переменных нет (например, локально), релиз соберётся неподписанным.
    val ksFile = System.getenv("KEYSTORE_FILE")?.let { file(it) }
    val hasKeystore = ksFile != null && ksFile.exists()
    signingConfigs {
        if (hasKeystore) {
            create("release") {
                storeFile = ksFile
                storePassword = System.getenv("KEYSTORE_PASSWORD")
                keyAlias = System.getenv("KEY_ALIAS")
                keyPassword = System.getenv("KEY_PASSWORD")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true      // R8: сжатие, оптимизация, обфускация
            isShrinkResources = true    // удаление неиспользуемых ресурсов
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
            if (hasKeystore) signingConfig = signingConfigs.getByName("release")
        }
    }
}

kotlin { jvmToolchain(17) }

dependencies {
    implementation(platform("androidx.compose:compose-bom:2025.10.01"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-core")
    implementation("androidx.activity:activity-compose:1.11.0")
    implementation("androidx.core:core-ktx:1.17.0")
    // Haze 1.7.x: последняя ветка 1.x (Kotlin 2.2.x). 2.0.x требует Kotlin 2.4.x, см. README
    implementation("dev.chrisbanes.haze:haze:1.7.2")
}
