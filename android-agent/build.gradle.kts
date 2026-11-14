/**
 * IonDomo - Build Configuration (Module)
 * Criado pela IONKOD | Fundador: Douglas Lúcio
 * Stack: Kotlin DSL, AndroidX Security, WorkManager, Retrofit
 */

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.ionkod.iondomo"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.ionkod.iondomo"
        minSdk = 26 // Android 8.0 (Oreo) - Necessário para APIs modernas de segurança e WorkManager
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = true // Ativa R8/ProGuard para ofuscação e redução de tamanho
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            isMinifyEnabled = false
        }
    }
    
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    
    kotlinOptions {
        jvmTarget = "17"
    }
    
    buildFeatures {
        viewBinding = true
        buildConfig = true
    }
}

dependencies {
    // Core Android
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")

    // IONKOD Security & Telemetry Stack
    // 1. EncryptedSharedPreferences (Armazenamento seguro de tokens e chaves)
    implementation("androidx.security:security-crypto:1.1.0-alpha06")
    
    // 2. WorkManager (Execução confiável de telemetria em background)
    implementation("androidx.work:work-runtime-ktx:2.9.0")
    
    // 3. Coroutines (Assincronismo)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    // 4. Network Stack (Retrofit + Gson) para comunicação com backend
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // Lifecycle (ViewModel, LiveData)
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")

    // Testing
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}