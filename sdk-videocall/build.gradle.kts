import com.neokarya.buildsrc.AndroidConfig
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
  alias(libs.plugins.android.library)
  alias(libs.plugins.kotlin.android)
  alias(libs.plugins.devtools.ksp)

  // plugin publikasi Maven
  `maven-publish`
}

android {
  namespace = "com.neokarya.sdk_videocall"
  compileSdk = AndroidConfig.compileSdk

  defaultConfig {
    minSdk = AndroidConfig.minSdk

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    consumerProguardFiles("consumer-rules.pro")
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

  publishing {
    singleVariant("release") { // Biasanya hanya memublikasikan varian 'release'
      withSourcesJar()
      withJavadocJar()
    }
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

// ----------------------------------------------------
// 🚀 KONFIGURASI PUBLISH KE JITPACK (Bagian Penting)
// ----------------------------------------------------
publishing {
  publications {
    create<MavenPublication>("release") {
      // Format yang digunakan JitPack: com.github.[User]:[Repo]:[Versi Tag]
      groupId = "com.github.kharozim"
      artifactId = "videocall"
      version = AndroidConfig.versionName // Versi ini akan diabaikan dan diganti oleh Tag Git Anda

      // Ini akan mengambil komponen 'release' yang berisi AAR yang di-generate Android
      afterEvaluate {
        from(components["release"])
      }
    }
  }
}


dependencies {

  implementation(libs.androidx.core.ktx)
  implementation(libs.androidx.appcompat)
  implementation(libs.material)
  implementation(libs.androidx.activity)
  implementation(libs.androidx.constraintlayout)
  testImplementation(libs.junit)
  androidTestImplementation(libs.androidx.junit)
  androidTestImplementation(libs.androidx.espresso.core)

  implementation(libs.androidx.fragment.ktx)

  implementation(libs.full.sdk)

  // network
  implementation(libs.retrofit)
  implementation(platform(libs.okhttp.bom))
  implementation(libs.okhttp)
  implementation(libs.logging.interceptor)
  implementation(libs.converter.gson)
}