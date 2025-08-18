import org.jetbrains.kotlin.gradle.dsl.ExplicitApiMode

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.serialization)
}

apply { from(rootProject.file("detekt/config.gradle")) }

kotlin {
    explicitApi = ExplicitApiMode.Strict
}

android {
    namespace = "io.promofire"
    compileSdk = 36

    defaultConfig {
        minSdk = 21

        buildConfigField(
            "String",
            "VERSION_NAME",
            "\"0.2.0\""
        )
    }

    buildFeatures {
        buildConfig = true
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_21.majorVersion
    }
}

dependencies {

    // Coroutines
    implementation(libs.kotlinx.coroutines)

    // Serialization
    implementation(libs.kotlinx.serialization)

    // Ktor
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.okhttp)
    implementation(libs.ktor.client.auth)
    implementation(libs.ktor.client.logging)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.serialization.kotlinx)

    // Test
    testImplementation(libs.junit)
}
