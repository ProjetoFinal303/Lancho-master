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
        dataBinding = true // Essencial para o layout do produto
    }
}

dependencies {
    // Padrão do Android
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation("androidx.recyclerview:recyclerview:1.3.2")

    // Jbcrypt para senhas (se ainda for usar em alguma parte)
    implementation("org.mindrot:jbcrypt:0.4")

    // Dependências para comunicação com Supabase e Stripe
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")

    // Dependências do Glide (para carregar imagens da internet)
    implementation("com.github.bumptech.glide:glide:4.16.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.16.0")

    // Dependências do Supabase
    implementation("io.github.jan-tennert.supabase:postgrest-kt:2.3.1")
    implementation("io.github.jan-tennert.supabase:gotrue-kt:2.3.1")
    implementation("io.ktor:ktor-client-android:2.3.8")

    // Dependências de Teste
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}