plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    id("kotlin-parcelize")
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "com.example.deeptraderspos"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.deeptraderspos"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0"

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

    buildFeatures {
        viewBinding = true
    }
    buildFeatures {
        dataBinding = true
    }

}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)



    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)


    // Navigation Component
    implementation("androidx.navigation:navigation-fragment-ktx:2.7.7")
    implementation("androidx.navigation:navigation-ui-ktx:2.7.7")

    //smooth bottom bar
    // implementation ("com.github.ibrahimsn98:SmoothBottomBar:1.7.9")

    // firebase
    implementation(platform("com.google.firebase:firebase-bom:33.3.0"))
    implementation("com.google.firebase:firebase-analytics")


    implementation("com.itextpdf:itext7-core:7.1.11")
    implementation("com.github.bumptech.glide:glide:4.16.0")




    // donut progress bar
    implementation("com.github.lzyzsd:circleprogress:1.2.1")

    // PDF generation
    implementation("com.itextpdf:itext7-core:7.1.11")

    // Image loading library
    implementation("com.github.bumptech.glide:glide:4.16.0")

    // Circular ImageView
    implementation("de.hdodenhof:circleimageview:3.1.0")

    // MPAndroidChart
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")

    // Donut progress bar
    implementation("com.github.lzyzsd:circleprogress:1.2.1")

    // Additional Material and ViewPager2 libraries
    implementation("com.google.android.material:material:1.8.0")
    implementation("androidx.viewpager2:viewpager2:1.0.0")

    // Protobuf (for Timestamp support in Firestore)
    implementation("com.google.protobuf:protobuf-javalite:3.21.12")



   implementation ("com.github.sd6352051:NiftyDialogEffects:v1.0.3")

    //monthpicker
  // implementation ("com.whiteelephant:monthandyearpicker:1.3.0")







}