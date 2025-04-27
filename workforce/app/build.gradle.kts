import java.util.Properties

val localProps = Properties().apply {
    // adjust path if your settings.gradle.kts lives elsewhere
    load(rootProject.file("local.properties").inputStream())
}


plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)

    kotlin("plugin.serialization") version  "2.1.20"
}

android {
    namespace = "com.cit.edu.workforce_hub"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.cit.edu.workforce_hub"
        minSdk = 30
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        val supabaseUrl     = localProps.getProperty("SUPABASE_URL")
        val supabaseAnonKey = localProps.getProperty("SUPABASE_ANON_KEY")
        val secret          = localProps.getProperty("SECRET")

        buildConfigField("String", "SUPABASE_URL",     "\"$supabaseUrl\"")
        buildConfigField("String", "SUPABASE_ANON_KEY","\"$supabaseAnonKey\"")
        buildConfigField("String", "SECRET",           "\"$secret\"")

    }
    buildFeatures {
        buildConfig = true
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
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

    implementation("io.github.jan-tennert.supabase:postgrest-kt:3.1.4")
    implementation("io.github.jan-tennert.supabase:storage-kt:3.1.4")
    implementation("io.github.jan-tennert.supabase:auth-kt:3.1.4")
    implementation("io.ktor:ktor-client-android:3.1.2")
    implementation("io.ktor:ktor-client-core:3.1.2")
    implementation("io.ktor:ktor-utils:3.1.2")
    implementation("io.github.jan-tennert.supabase:gotrue-kt:2.3.0")
}