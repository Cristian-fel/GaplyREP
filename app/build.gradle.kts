plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.gaply.app"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.gaply.app"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
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

    buildFeatures {
        compose = true
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2024.09.00")
    implementation(composeBom)

    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.activity:activity-compose:1.9.2")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.6")
    implementation("androidx.navigation:navigation-compose:2.8.1")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")

    // Carga y procesamiento de imágenes para Jetpack Compose (Vista previa)
    implementation("io.coil-kt:coil-compose:2.6.0")

    implementation(platform("com.google.firebase:firebase-bom:33.7.0"))
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.google.firebase:firebase-storage")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.8.1")

    // Radar AR: vista previa de cámara en vivo (CameraX) y GPS del dispositivo.
    // listenablefuture / guava: API que CameraX expone (ProcessCameraProvider).
    implementation("androidx.camera:camera-core:1.3.4")
    implementation("androidx.camera:camera-camera2:1.3.4")
    implementation("androidx.camera:camera-lifecycle:1.3.4")
    implementation("androidx.camera:camera-view:1.3.4")
    implementation("com.google.guava:guava:32.1.3-android")
    implementation("com.google.guava:listenablefuture:9999.0-empty-to-avoid-conflict-with-guava")
    implementation("com.google.android.gms:play-services-location:21.3.0")

    debugImplementation("androidx.compose.ui:ui-tooling")

    // Pruebas unitarias del agente (JVM puro: corren sin emulador ni dispositivo)
    testImplementation("junit:junit:4.13.2")
}

// Muestra cada prueba en la terminal (evidencia para la sustentación de IA)
tasks.withType<Test>().configureEach {
    testLogging {
        events("passed", "failed", "skipped")
    }
}

// Evita duplicidad de ListenableFuture cuando Guava está en el classpath
configurations.all {
    resolutionStrategy.dependencySubstitution {
        substitute(module("com.google.guava:listenablefuture"))
            .using(module("com.google.guava:listenablefuture:9999.0-empty-to-avoid-conflict-with-guava"))
    }
}