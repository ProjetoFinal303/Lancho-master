plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.projetofinal"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.projetofinal"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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

    buildFeatures {
        viewBinding = true // Already enabled, which is great!
        dataBinding = true // Also enabled, used by ProdutoAdapter
    }
}

dependencies {
    implementation("androidx.recyclerview:recyclerview:1.3.2") // Consider updating to latest stable
    implementation("org.mindrot:jbcrypt:0.4")
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation("com.google.code.gson:gson:2.10.1")
    // Supabase
    implementation("io.github.jan-tennert.supabase:postgrest-kt:2.3.1")
    implementation("io.github.jan-tennert.supabase:gotrue-kt:2.3.1")
    implementation("io.ktor:ktor-client-android:2.3.8")
    //dependencia do OkHttp
    implementation("com.squareup.okhttp3:okhttp:4.12.0")



    // Room dependencies were present but DatabaseHelper is custom SQLite.
    // Sticking to custom SQLite as per analysis.
    // implementation(libs.room.common)
    // implementation(libs.room.runtime)
    // annotationProcessor(libs.room.compiler)

    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}