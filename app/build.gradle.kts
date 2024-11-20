plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.example.subscriberapp"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.subscriberapp"
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }

    packagingOptions { resources.excludes.add("META-INF/*") }
}

dependencies {
    implementation("com.hivemq:hivemq-mqtt-client:1.3.3")
    implementation ("androidx.localbroadcastmanager:localbroadcastmanager:1.0.0")
    implementation ("org.eclipse.paho:org.eclipse.paho.android.service:1.1.1") // MQTT Android service
    implementation ("androidx.sqlite:sqlite:2.1.0") // SQLite support
    implementation ("com.google.android.gms:play-services-maps:18.0.2") // Google Maps
    implementation ("com.google.android.gms:play-services-location:19.0.1")
    implementation ("com.google.android.material:material:1.4.0") // Material Components
    implementation ("com.google.code.gson:gson:2.8.9") // or the latest version
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}