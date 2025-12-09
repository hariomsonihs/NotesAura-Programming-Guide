plugins {
    id("com.android.application") version libs.versions.agp
    id("com.google.gms.google-services") version "4.4.2" // Latest version
    id("com.google.firebase.crashlytics") version "3.0.2" // For Crashlytics
}

android {
    namespace = "com.hariomsonihs.notesaura"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.hariomsonihs.notesaura"
        minSdk = 24
        targetSdk = 36
        versionCode = 10
        versionName = "10.0.hs"

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
}

dependencies {
    // Markdown rendering
    implementation("io.noties.markwon:core:4.6.2")

    // AndroidX
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.fragment)
    implementation(libs.recyclerview)
    implementation(libs.cardview)
    implementation(libs.swiperefreshlayout)

    // Firebase (BOM for version management)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.storage)
    implementation(libs.firebase.crashlytics)
    implementation(libs.firebase.inappmessaging)
    implementation("com.google.firebase:firebase-messaging")

    // Navigation
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)

    // Image loading
    implementation(libs.glide)
    implementation(libs.circleimageview)

    // Animations
    implementation(libs.lottie)



    
    // PhotoView for zoom functionality
    implementation("com.github.chrisbanes:PhotoView:2.3.0")
    
    // PDF text extraction for search
    implementation("com.tom-roush:pdfbox-android:2.0.27.0")
    
    // Other dependencies
    implementation("com.razorpay:checkout:1.6.40") // Latest version
    implementation("com.google.code.gson:gson:2.11.0") // Latest version

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}