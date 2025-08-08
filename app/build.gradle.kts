plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.googleService)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.wilsoft.arki_streamconfig"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.wilsoft.arki_streamconfig"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3) // Añadir Material para el CircularProgressIndicator
    implementation(platform(libs.firebaseBom))
    implementation(libs.firebaseAuthKtx)
    implementation(libs.firebaseFirestoreKtx)
    implementation(libs.firebaseStorageKtx)
    implementation(libs.firebaseDatabaseKtx) // Firebase Realtime Database
    implementation(libs.materialIconsExtended) // Usar la dependencia del archivo libs.versions.toml
    implementation(libs.androidx.navigation.compose)

    implementation(platform(libs.androidx.compose.bom)) // Tu BOM de Compose
    implementation(libs.androidx.material.icons.extended) // Los íconos extendidos, si los usas
    implementation(libs.androidx.ui) // Parte de Compose UI
    implementation(libs.androidx.ui.tooling) // Herramientas de Compose
    implementation(libs.androidx.ui.tooling.preview) // Previews de Compose
    implementation(libs.androidx.navigation.compose) // Para la navegación en Compose
    implementation(libs.coil.compose)  // Esto incluirá Coil Compose para cargar imágenes
    implementation(libs.androidx.compose.foundation)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    implementation(libs.kotlinx.serialization.json)

}