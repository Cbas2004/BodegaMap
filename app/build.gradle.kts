plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.sebas.bodegamap"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.sebas.bodegamap"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Definida en gradle.properties (no versionado). Fallback al alias
        // que usa el emulador de Android para referirse al host local.
        val apiBaseUrl = providers.gradleProperty("API_BASE_URL")
            .getOrElse("http://10.0.2.2:8080/")
        buildConfigField("String", "API_BASE_URL", "\"$apiBaseUrl\"")
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
        buildConfig = true
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
    // Íconos de Material (lupa, limpiar): gestionado por el BOM de Compose ya aplicado arriba.
    implementation("androidx.compose.material:material-icons-core")
    implementation(libs.androidx.navigation.compose)
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

    // Ubicación del usuario (última ubicación conocida para centrar el mapa)
    implementation("com.google.android.gms:play-services-location:21.0.1")

    // Coil: carga de imágenes asíncrona para Compose (imágenes de productos)
    implementation("io.coil-kt:coil-compose:2.7.0")

    //Dependencia de Google Fonts
    implementation("androidx.compose.ui:ui-text-google-fonts:1.7.0")

    // Room: caché local de bodegas (respaldo cuando no hay red)
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")

    // Splash screen: mismo comportamiento en Android 12+ (API nativa) y
    // versiones anteriores (la librería lo emula), en vez de dos caminos.
    implementation("androidx.core:core-splashscreen:1.0.1")
}
