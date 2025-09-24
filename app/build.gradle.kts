import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("com.google.gms.google-services")
    alias(libs.plugins.ksp) // Added

}

android {
    namespace = "com.mzansi.recipes"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.mzansi.recipes"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Explicitly load local.properties
        val localProps = Properties() // Use imported Properties
        val localPropsFile = rootProject.file("local.properties")
        var apiKeyFromLocalProps = "" // Default to empty
        if (localPropsFile.exists() && localPropsFile.isFile) { // Corrected: isFile property
            try {
                // Explicitly type the stream parameter
                localPropsFile.inputStream().use { stream: java.io.InputStream -> 
                    localProps.load(stream) 
                }
                apiKeyFromLocalProps = localProps.getProperty("RAPIDAPI_KEY") ?: ""
            } catch (e: Exception) {
                println("Error loading local.properties: ${e.message}")
            }
        } else {
            println("local.properties file not found at ${localPropsFile.absolutePath}")
        }
        
        println("RAPIDAPI_KEY (from explicit load): '$apiKeyFromLocalProps'")

        // RapidAPI Tasty key from local.properties
        buildConfigField(
            "String",
            "RAPIDAPI_KEY",
            "\"$apiKeyFromLocalProps\"" // Use the explicitly loaded key
        )
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

    buildFeatures {
        buildConfig = true // <--- MODIFIED BACK TO TRUE
        compose = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }



    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.14"
    }

    packaging {
        resources.excludes += "/META-INF/{AL2.0,LGPL2.1}"
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

    // Splash Screen
    implementation(libs.androidx.core.splashscreen) // Added this line

    // Firebase & Google Sign-In
    implementation(platform("com.google.firebase:firebase-bom:33.0.0"))
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.firebase:firebase-firestore-ktx")
    implementation("com.google.firebase:firebase-messaging-ktx") // Added for FCM
    implementation("com.google.android.gms:play-services-auth:21.2.0") // Added for Google Sign-In


    // Retrofit + Moshi
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-moshi:2.9.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    implementation("com.squareup.moshi:moshi-kotlin:1.15.1")

    // Room
    implementation("androidx.room:room-ktx:2.6.1")
    implementation(libs.androidx.navigation.runtime.ktx)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.palette.ktx)
    implementation(libs.androidx.appcompat)
    // kapt("androidx.room:room-compiler:2.6.1") // Changed
    ksp("androidx.room:room-compiler:2.6.1") // To KSP
    implementation(libs.androidx.navigation.compose)



    // DataStore
    implementation("androidx.datastore:datastore-preferences:1.1.7")

    // WorkManager
    implementation("androidx.work:work-runtime-ktx:2.10.4")

    // Coil for image loading
    implementation("io.coil-kt:coil-compose:2.7.0")

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)


    implementation("androidx.compose.material:material-icons-core")

    implementation("androidx.compose.material:material-icons-extended")
    implementation("com.squareup.retrofit2:converter-moshi:2.9.0")
}
