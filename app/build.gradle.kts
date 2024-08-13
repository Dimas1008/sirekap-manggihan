plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "com.example.mysirekapmanggihan"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.mysirekapmanggihan"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures {
        viewBinding = true
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
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.firebase.database)
    implementation(libs.firebase.storage)
    implementation(libs.firebase.auth.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    implementation(platform("com.google.firebase:firebase-bom:33.1.1"))
    implementation("com.google.firebase:firebase-analytics")

    implementation("androidx.navigation:navigation-fragment-ktx:2.3.5")
    implementation("androidx.navigation:navigation-fragment:2.7.0")

    implementation("androidx.navigation:navigation-common:2.7.0")
    implementation("androidx.navigation:navigation-common-ktx:2.7.0")

    implementation("androidx.navigation:navigation-runtime:2.7.0")
    implementation("androidx.navigation:navigation-runtime-ktx:2.7.0")

    implementation("androidx.navigation:navigation-ui-ktx:2.7.0")
    implementation("androidx.navigation:navigation-ui:2.7.0")

    implementation ("com.github.bumptech.glide:glide:4.12.0")
    annotationProcessor ("com.github.bumptech.glide:compiler:4.12.0")

    implementation("com.github.bumptech.glide:glide:4.15.1")
    implementation ("com.github.smarteist:autoimageslider:1.4.0")

    implementation ("com.github.denzcoskun:ImageSlideshow:0.1.0")
    implementation ("com.github.PhilJay:MPAndroidChart:v3.1.0")
}