import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.compose.compiler)
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
        versionCode = 7
        versionName = "2.0"

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
            isShrinkResources = true

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
        buildConfig = true
        resValues = true
        compose = true
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.documentfile)
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.play.services.ads)
    implementation(libs.androidx.core.splashscreen)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    debugImplementation(libs.androidx.compose.ui.tooling)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.core)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.activity.compose)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
