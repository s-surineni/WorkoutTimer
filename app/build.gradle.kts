import com.github.triplet.gradle.androidpublisher.ReleaseStatus
import java.io.FileInputStream
import java.util.Properties

plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.compose.compiler)
  alias(libs.plugins.kotlin.serialization)
  alias(libs.plugins.ksp)
  alias(libs.plugins.gpp)
}

val keystorePropertiesFile = rootProject.file("keystore.properties")
val keystoreProperties = Properties()
if (keystorePropertiesFile.exists()) {
    try {
        keystoreProperties.load(FileInputStream(keystorePropertiesFile))
    } catch (_: Exception) {}
}

val releaseStoreFilePath = keystoreProperties.getProperty("storeFile")?.takeIf { it.isNotBlank() }
    ?: "workouttimer-upload-key.jks"
val releaseStoreFile = if (releaseStoreFilePath.startsWith("/")) file(releaseStoreFilePath) else rootProject.file(releaseStoreFilePath)
val releaseStorePassword = keystoreProperties.getProperty("storePassword")?.takeIf { it.isNotBlank() }
    ?: System.getenv("KEYSTORE_PASSWORD")
    ?: "workouttimerInternalPass2026"
val releaseKeyAlias = keystoreProperties.getProperty("keyAlias")?.takeIf { it.isNotBlank() }
    ?: System.getenv("KEY_ALIAS")
    ?: "workouttimer_upload"
val releaseKeyPassword = keystoreProperties.getProperty("keyPassword")?.takeIf { it.isNotBlank() }
    ?: System.getenv("KEY_PASSWORD")
    ?: "workouttimerInternalPass2026"

play {
  track.set("internal")
  releaseStatus.set(ReleaseStatus.COMPLETED)
  val serviceAccountFile = file("play-service-account.json")
  val rootServiceAccountFile = rootProject.file("play-service-account.json")
  if (serviceAccountFile.exists()) {
    serviceAccountCredentials.set(serviceAccountFile)
  } else if (rootServiceAccountFile.exists()) {
    serviceAccountCredentials.set(rootServiceAccountFile)
  }
}

android {
    namespace = "com.example.workouttimer"
    compileSdk = 36
    defaultConfig {
        applicationId = "sampath.workouttimer"
        minSdk = 24
        targetSdk = 36
        versionCode = 4
        versionName = "1.0.3"
    }

    signingConfigs {
        create("release") {
            if (releaseStoreFile.exists()) {
                storeFile = releaseStoreFile
                storePassword = releaseStorePassword
                keyAlias = releaseKeyAlias
                keyPassword = releaseKeyPassword
            }
        }
    }

    buildTypes {
        debug {
            enableUnitTestCoverage = true
        }
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            if (releaseStoreFile.exists()) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
      compose = true
      aidl = false
      buildConfig = true
      shaders = false
    }

    packaging {
      resources {
        excludes += "/META-INF/{AL2.0,LGPL2.1}"
      }
    }
}

kotlin {
    jvmToolchain(17)
}

dependencies {
  val composeBom = platform(libs.androidx.compose.bom)
  implementation(composeBom)
  androidTestImplementation(composeBom)

  // Core Android dependencies
  implementation(libs.androidx.core.ktx)
  implementation(libs.androidx.lifecycle.runtime.ktx)
  implementation(libs.androidx.activity.compose)

  // Arch Components
  implementation(libs.androidx.lifecycle.runtime.compose)
  implementation(libs.androidx.lifecycle.viewmodel.compose)

  // Compose
  implementation(libs.androidx.compose.ui)
  implementation(libs.androidx.compose.ui.tooling.preview)
  implementation(libs.androidx.compose.material3)
  // Material icons (extended) for Compose
  implementation("androidx.compose.material:material-icons-extended")
  // Tooling
  debugImplementation(libs.androidx.compose.ui.tooling)
  // Instrumented tests
  androidTestImplementation(libs.androidx.compose.ui.test.junit4)
  debugImplementation(libs.androidx.compose.ui.test.manifest)

  // Local tests: jUnit, coroutines, Android runner
  testImplementation(libs.junit)
  testImplementation(libs.kotlinx.coroutines.test)

  // Instrumented tests: jUnit rules and runners
  androidTestImplementation(libs.androidx.test.core)
  androidTestImplementation(libs.androidx.test.ext.junit)
  androidTestImplementation(libs.androidx.test.runner)
  androidTestImplementation(libs.androidx.test.espresso.core)

  // Navigation
  implementation(libs.androidx.navigation3.ui)
  implementation(libs.androidx.navigation3.runtime)
  implementation(libs.androidx.lifecycle.viewmodel.navigation3)

  // Serialization
  implementation(libs.kotlinx.serialization.json)

  // Room Database
  implementation(libs.androidx.room.runtime)
  implementation(libs.androidx.room.ktx)
  ksp(libs.androidx.room.compiler)
}
