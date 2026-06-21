import org.jetbrains.kotlin.konan.properties.Properties
import java.io.FileInputStream

plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.kotlin.compose)
  alias(libs.plugins.kotlin.serialization)
}

val keystorePropertiesFile = rootProject.file("key.properties")
val keystoreProperties = Properties()
keystoreProperties.load(FileInputStream(keystorePropertiesFile))

// Development
val secretDevPropertiesFile = project.rootProject.file("secret.dev.properties")
val secretDevProperties = Properties()
secretDevProperties.load(FileInputStream(secretDevPropertiesFile))

// Preview
val secretPreviewPropertiesFile = project.rootProject.file("secret.preview.properties")
val secretPreviewProperties = Properties()
secretPreviewProperties.load(FileInputStream(secretPreviewPropertiesFile))

// Production
val secretProdPropertiesFile = project.rootProject.file("secret.production.properties")
val secretProdProperties = Properties()
secretProdProperties.load(FileInputStream(secretProdPropertiesFile))


android {
  namespace = "com.freyza.employee"
  compileSdk = 37

  defaultConfig {
    applicationId = "com.freyza.employee"
    minSdk = 24
    targetSdk = 36
    versionCode = 1
    versionName = "1.0.0"

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

    buildConfigField("String", "SUPABASE_PUBLISHABLE_KEY", "\"\"")
    buildConfigField("String", "SUPABASE_URL", "\"\"")
    buildConfigField("String", "WEB_CLIENT_ID", "\"\"")
    buildConfigField("String", "API_URL", "\"\"")
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
      resValue("string", "app_name", "Freyza Employee (Dev)")

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
      buildConfigField(
        "String",
        "API_URL",
        "\"${secretDevProperties.getProperty("API_URL")}\""
      )
    }

    create("preview") {
      dimension = "env"
      applicationIdSuffix = ".preview"
      versionNameSuffix = "-pre"
      resValue("string", "app_name", "Freyza Employee (Preview)")

      buildConfigField(
        "String",
        "SUPABASE_PUBLISHABLE_KEY",
        "\"${secretPreviewProperties.getProperty("SUPABASE_PUBLISHABLE_KEY")}\""
      )
      buildConfigField(
        "String",
        "SUPABASE_URL",
        "\"${secretPreviewProperties.getProperty("SUPABASE_URL")}\""
      )
      buildConfigField(
        "String",
        "WEB_CLIENT_ID",
        "\"${secretPreviewProperties.getProperty("WEB_CLIENT_ID")}\""
      )
      buildConfigField(
        "String",
        "API_URL",
        "\"${secretPreviewProperties.getProperty("API_URL")}\""
      )
    }

    create("prod") {
      dimension = "env"

      buildConfigField(
        "String",
        "SUPABASE_PUBLISHABLE_KEY",
        "\"${secretProdProperties.getProperty("SUPABASE_PUBLISHABLE_KEY")}\""
      )
      buildConfigField(
        "String",
        "SUPABASE_URL",
        "\"${secretProdProperties.getProperty("SUPABASE_URL")}\""
      )
      buildConfigField(
        "String",
        "WEB_CLIENT_ID",
        "\"${secretProdProperties.getProperty("WEB_CLIENT_ID")}\""
      )
      buildConfigField(
        "String",
        "API_URL",
        "\"${secretProdProperties.getProperty("API_URL")}\""
      )
    }
  }

  buildTypes {
    getByName("debug") {
      isDebuggable = true
      versionNameSuffix = "-debug"
    }
    getByName("release") {
      isMinifyEnabled = true
      isShrinkResources = true
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

  buildFeatures {
    compose = true
    buildConfig = true
    resValues = true
  }

  buildToolsVersion = "36.0.0"
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
  implementation(libs.ktor.client.content.negotiation)
  implementation(libs.ktor.serialization.kotlinx.json)
  implementation(libs.ktor.client.logging)

  // google login
  implementation(libs.googleid)
  implementation(libs.androidx.credentials)
  implementation(libs.androidx.credentials.play.services.auth)

  implementation(libs.kizitonwose.calendar)
  implementation(libs.timber)
}
