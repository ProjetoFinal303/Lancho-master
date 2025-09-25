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
        viewBinding = true
        dataBinding = true // Necessário por causa de um dos seus layouts
    }
}

dependencies {
    // Padrão do Android
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0") // <-- ADICIONADO AQUI

    implementation("de.hdodenhof:circleimageview:3.1.0")
    // Google Sign-In
    implementation("com.google.android.gms:play-services-auth:21.2.0")
    implementation("com.stripe:stripe-android:20.34.0")
    // Bibliotecas Essenciais de Rede e JSON
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.google.code.gson:gson:2.10.1")

    // Glide (para carregar imagens)
    implementation("com.github.bumptech.glide:glide:4.16.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.16.0")

    // Testes
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}