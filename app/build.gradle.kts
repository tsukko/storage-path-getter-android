import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
}

// Keystore properties logic: Using project.file for better portability
val keystorePropertiesFile = rootProject.file("../keystore.properties")
val keystoreProperties = Properties().apply {
    if (keystorePropertiesFile.exists()) {
        keystorePropertiesFile.inputStream().use { load(it) }
    }
}

android {
    namespace = "jp.co.integrityworks.storagepathgetter"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "jp.co.integrityworks.storagepathgetter"
        minSdk = libs.versions.minSdk.get().toInt()
        targetSdk = libs.versions.targetSdk.get().toInt()
        versionCode = 6
        versionName = "1.3"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        create("release") {
            // Safe property access with default values or early exit
            keyAlias = keystoreProperties["keyAlias"] as? String ?: ""
            keyPassword = keystoreProperties["keyPassword"] as? String ?: ""
            storeFile = keystoreProperties["storeFile"]?.let { file(it as String) }
            storePassword = keystoreProperties["storePassword"] as? String ?: ""
        }
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".deb"
            isMinifyEnabled = false
            
            // Modern property access with explicit error reporting
            val admobAppId = project.findProperty("admobAppIdSample") as? String 
                ?: throw GradleException("Property 'admobAppIdSample' is missing in gradle.properties or local.properties")
            val admobBannerId = project.findProperty("admobBannerSample") as? String 
                ?: throw GradleException("Property 'admobBannerSample' is missing in gradle.properties or local.properties")

            manifestPlaceholders["admob_app_id"] = admobAppId
            buildConfigField("String", "admob_app_id", "\"$admobAppId\"")
            resValue("string", "ad_unit_id", admobBannerId)
        }
        release {
            isMinifyEnabled = true
            
            // Modern property access with explicit error reporting for release builds
            val admobAppId = project.findProperty("admobAppIdStoragePath") as? String 
                ?: throw GradleException("Property 'admobAppIdStoragePath' is missing! Required for release builds.")
            val admobBannerId = project.findProperty("admobBannerStoragePath") as? String 
                ?: throw GradleException("Property 'admobBannerStoragePath' is missing! Required for release builds.")

            manifestPlaceholders["admob_app_id"] = admobAppId
            buildConfigField("String", "admob_app_id", "\"$admobAppId\"")
            resValue("string", "ad_unit_id", admobBannerId)
            
            signingConfig = signingConfigs.getByName("release")
            
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    buildFeatures {
        viewBinding = true
        buildConfig = true
        resValues = true
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.play.services.ads)
    
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
