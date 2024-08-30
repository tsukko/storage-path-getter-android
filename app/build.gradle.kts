
import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
}

var keystoreProperties = Properties()
var keystorePropertiesFile = rootProject.file("../keystore.properties")
if (keystorePropertiesFile.exists()) {
    keystoreProperties.load(FileInputStream(keystorePropertiesFile))
}

android {
    namespace = "jp.co.integrityworks.storagepathgetter"
    compileSdk = 35

    defaultConfig {
        applicationId = "jp.co.integrityworks.storagepathgetter"
        minSdk = 29
        targetSdk = 35
        versionCode = 3
        versionName = "1.2"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        create("release") {
            keyAlias = keystoreProperties["keyAlias"] as String
            keyPassword = keystoreProperties["keyPassword"] as String
            storeFile = file(keystoreProperties["storeFile"] as String)
            storePassword = keystoreProperties["storePassword"] as String
        }
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".deb"
            isMinifyEnabled = false
            manifestPlaceholders["admob_app_id"] = project.properties["admobAppIdSample"] as String
            buildConfigField(
                "String",
                "admob_app_id",
                '"' + "${project.properties["admobAppIdSample"] ?: ""}" + '"'
            )
            resValue(
                "string",
                "ad_unit_id",
                project.properties["admobBannerSample"] as String
            )
        }
        release {
            isMinifyEnabled = true
            manifestPlaceholders["admob_app_id"] = project.properties["admobAppIdStoragePath"] as String
            buildConfigField(
                "String",
                "admob_app_id",
                '"' + "${project.properties["admobAppIdStoragePath"] ?: ""}" + '"'
            )
            resValue(
                "string",
                "ad_unit_id",
                project.properties["admobBannerStoragePath"] as String
            )
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    buildFeatures {
        viewBinding = true
        buildConfig = true
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
    implementation(libs.play.services.ads)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}