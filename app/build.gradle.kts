plugins {
    id("com.android.application")
    id("com.google.gms.google-services")
}

android {
    namespace = "sandeep.kycverification"
    compileSdk = 34

    packaging {
        resources {
            // Exclude both LICENSE.md and NOTICE.md files
            excludes += setOf("META-INF/LICENSE.md", "META-INF/NOTICE.md")
        }
    }

    defaultConfig {
        applicationId = "sandeep.kycverification"
        minSdk = 24
        targetSdk = 34
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
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.firebase.firestore)
    implementation(libs.android.mail)
    implementation(libs.android.activation)
    implementation(libs.firebase.auth)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}
