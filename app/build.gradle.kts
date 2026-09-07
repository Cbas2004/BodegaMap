plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)

}

android {
    namespace = "com.sebas.bodegamap"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.sebas.bodegamap"
        minSdk = 26
        targetSdk = 36
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
        compose = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    // Retrofit: conecta Android con backend
    implementation("com.squareup.retrofit2:retrofit:2.9.0")

    // Converter Gson: convierte JSON a objetos kotlin
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")

    // Logs HTTP : muestra requests en Logcat
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    //Dependencias de ViewModel
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.7")

    //Corrutinas
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")

    //Dependencia MapBox
    implementation("com.mapbox.maps:android:11.4.1")

    // Boxicons para Compose (reemplaza Material Icons)
    implementation(libs.boxicons)
}
