import org.jetbrains.kotlin.gradle.dsl.ExplicitApiMode

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.maven.publish)
}

apply { from(rootProject.file("detekt/config.gradle")) }

kotlin {
    explicitApi = ExplicitApiMode.Strict
}

val sdkVersion = providers.gradleProperty("VERSION_NAME").get()

android {
    namespace = "io.promofire"
    compileSdk = 36

    defaultConfig {
        minSdk = 24

        buildConfigField("String", "VERSION_NAME", "\"$sdkVersion\"")
    }

    buildFeatures {
        buildConfig = true
    }

    testOptions {
        // android.util.Log и Build недоступны в JVM-тестах; без этого
        // любой вызов логгера падает с "not mocked".
        unitTests.isReturnDefaultValues = true
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
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.serialization.kotlinx)

    // Test
    testImplementation(libs.junit)
    testImplementation(libs.ktor.client.mock)
    testImplementation(libs.kotlinx.coroutines.test)
}

// Публикация. До релиза SDK ставится в демо из локального репозитория:
//
//   ./gradlew :promofire:publishToMavenLocal
//
// Это тот же артефакт, что уедет в Maven Central, поэтому ошибки упаковки
// видны сразу, а не в день релиза.
mavenPublishing {
    coordinates("io.promofire", "sdk", sdkVersion)

    publishToMavenCentral()

    // Подпись требуется только Maven Central. Ключ появится к публикации;
    // publishToMavenLocal без него работает.
    if (providers.gradleProperty("signingInMemoryKey").isPresent) {
        signAllPublications()
    }

    pom {
        name.set("Promofire SDK")
        description.set("Referral and promo codes for Android apps.")
        url.set("https://github.com/Octy-LLC/promofire-android-sdk")

        // Лицензии пока нет: это решение владельца, и без неё Maven Central
        // публикацию не примет.

        developers {
            developer {
                id.set("octy-llc")
                name.set("Octy LLC")
                url.set("https://github.com/Octy-LLC")
            }
        }

        scm {
            url.set("https://github.com/Octy-LLC/promofire-android-sdk")
            connection.set("scm:git:https://github.com/Octy-LLC/promofire-android-sdk.git")
            developerConnection.set("scm:git:ssh://git@github.com/Octy-LLC/promofire-android-sdk.git")
        }
    }
}

// Живой набор (`LiveTest`) ходит в настоящий бэкенд, поэтому в обычный прогон
// он не входит: `:promofire:test` обязан проходить без сети и без тенанта.
//
//   ./gradlew :promofire:testDebugUnitTest -Ppromofire.live \
//     -Ppromofire.secret=<ANDROID-секрет> [-Ppromofire.url=http://127.0.0.1:3000]
//
// Адрес и секрет передаются свойствами Gradle, а не через окружение: демон
// переживает оболочку, из которой его запустили, и тесты получили бы
// прошлый экспорт вместо нынешнего.
val liveTests = providers.gradleProperty("promofire.live").isPresent

tasks.withType<Test>().configureEach {
    if (!liveTests) {
        exclude("io/promofire/LiveTest*")
        return@configureEach
    }

    providers.gradleProperty("promofire.secret").orNull?.let {
        environment("PROMOFIRE_E2E_SECRET", it)
    }
    providers.gradleProperty("promofire.url").orNull?.let {
        environment("PROMOFIRE_E2E_URL", it)
    }

    // Результат зависит от состояния бэкенда, а не от исходников: иначе второй
    // запуск объявляется актуальным и молча ничего не проверяет.
    outputs.upToDateWhen { false }

    testLogging { events("passed", "skipped", "failed") }
}
