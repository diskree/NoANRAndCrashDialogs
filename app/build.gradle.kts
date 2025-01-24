plugins {
    alias(libs.plugins.agp.app)
    alias(libs.plugins.kotlin)
}

android {
    namespace = "com.diskree.noanrandcrashdialogs"
    compileSdk = 35

    defaultConfig {
        minSdk = 35
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles("proguard-rules.pro")
            signingConfig = signingConfigs["debug"]
        }
    }

    kotlin {
        jvmToolchain(17)
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    packaging {
        resources {
            merges += "META-INF/xposed/*"
            excludes += "**"
        }
    }

    lint {
        abortOnError = true
        checkReleaseBuilds = false
    }
}

dependencies {
    compileOnly(libs.libxposed.api)
    implementation(libs.libxposed.service)
}
