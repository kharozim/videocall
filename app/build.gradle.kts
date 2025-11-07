import com.neokarya.buildsrc.AndroidConfig
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.kotlin.android)
  alias(libs.plugins.devtools.ksp)
}

android {
  namespace = "com.neokarya.sdk"
  compileSdk = AndroidConfig.compileSdk

  defaultConfig {
    applicationId = "com.neokarya.sdk"
    minSdk = AndroidConfig.minSdk
    targetSdk = AndroidConfig.targetSdk
    versionCode = AndroidConfig.versionCode
    versionName = AndroidConfig.versionName

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
  }

  buildTypes {
    release {
      isMinifyEnabled = true
      proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
    }
  }
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
  }

  buildFeatures {
    viewBinding = true
  }
}

kotlin {
  compilerOptions {
    jvmTarget = JvmTarget.fromTarget(AndroidConfig.jvmTarget)
  }
}

dependencies {

  implementation(project(":sdk-videocall"))

  implementation(libs.androidx.core.ktx)
  implementation(libs.androidx.appcompat)
  implementation(libs.material)
  implementation(libs.androidx.activity)
  implementation(libs.androidx.constraintlayout)
  testImplementation(libs.junit)
  androidTestImplementation(libs.androidx.junit)
  androidTestImplementation(libs.androidx.espresso.core)
}