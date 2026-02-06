import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.konan.properties.Properties
import java.io.FileInputStream

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
}

val keystorePropertiesFile = rootProject.file("key.properties")
val keystoreProperties = Properties()
keystoreProperties.load(FileInputStream(keystorePropertiesFile))

val secretDevPropertiesFile = project.rootProject.file("secret.dev.properties")
val secretDevProperties = Properties()
secretDevProperties.load(FileInputStream(secretDevPropertiesFile))

val secretStagingPropertiesFile = project.rootProject.file("secret.staging.properties")
val secretStagingProperties = Properties()
secretStagingProperties.load(FileInputStream(secretStagingPropertiesFile))

android {
    namespace = "com.freyza.employee"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.freyza.employee"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField("String", "SUPABASE_PUBLISHABLE_KEY", "")
        buildConfigField("String", "SUPABASE_URL", "")
        buildConfigField("String", "WEB_CLIENT_ID", "")
    }

    signingConfigs {
        create("release") {
            keyAlias = keystoreProperties["keyAlias"] as String
            keyPassword = keystoreProperties["keyPassword"] as String
            storeFile = file(keystoreProperties["storeFile"] as String)
            storePassword = keystoreProperties["storePassword"] as String
        }
    }

    flavorDimensions += "env"

    productFlavors {
        create("dev") {
            dimension = "env"
            applicationIdSuffix = ".dev"
            versionNameSuffix = "-dev"

            buildConfigField(
                "String",
                "SUPABASE_PUBLISHABLE_KEY",
                "\"${secretDevProperties.getProperty("SUPABASE_PUBLISHABLE_KEY")}\""
            )
            buildConfigField(
                "String",
                "SUPABASE_URL",
                "\"${secretDevProperties.getProperty("SUPABASE_URL")}\""
            )
            buildConfigField(
                "String",
                "WEB_CLIENT_ID",
                "\"${secretDevProperties.getProperty("WEB_CLIENT_ID")}\""
            )
        }

        create("staging") {
            dimension = "env"
            applicationIdSuffix = ".staging"
            versionName = "-staging"

            buildConfigField(
                "String",
                "SUPABASE_PUBLISHABLE_KEY",
                "\"${secretStagingProperties.getProperty("SUPABASE_PUBLISHABLE_KEY")}\""
            )
            buildConfigField(
                "String",
                "SUPABASE_URL",
                "\"${secretStagingProperties.getProperty("SUPABASE_URL")}\""
            )
            buildConfigField(
                "String",
                "WEB_CLIENT_ID",
                "\"${secretStagingProperties.getProperty("WEB_CLIENT_ID")}\""
            )
        }

        create("prod") {
            dimension = "env"
        }
    }

    buildTypes {
        getByName("debug") {
            isDebuggable = true
            versionNameSuffix = "-debug"
        }
        getByName("release") {
            isMinifyEnabled = true
            isDebuggable = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )

            signingConfig = signingConfigs.getByName("release")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlin {
        compilerOptions {
            jvmTarget = JvmTarget.fromTarget("17")
        }
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    buildToolsVersion = "35.0.0"
}

dependencies {
    // core android and compose
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.runtime.livedata)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    // compose navigation
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.androidx.navigation.compose)

    // koin DI
    implementation(platform(libs.koin.bom))
    implementation(libs.koin.core)
    implementation(libs.koin.android)
    implementation(libs.koin.compose)

    // supabase
    implementation(platform(libs.supabase.bom))
    implementation(libs.auth.kt)
    implementation(libs.postgrest.kt)

    implementation(libs.ktor.client.android)

    // google login
    implementation(libs.googleid)
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)

    implementation(libs.kizitonwose.calendar)
//    implementation(libs.timber)
}
