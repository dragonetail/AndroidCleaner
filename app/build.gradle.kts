plugins {
    alias(libs.plugins.android.application)
    id("idea")
}

android {
    namespace = "com.blackharry.androidcleaner"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.blackharry.androidcleaner"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        javaCompileOptions {
            annotationProcessorOptions {
                arguments += mapOf(
                    "room.schemaLocation" to "$projectDir/schemas",
                    "room.incremental" to "true",
                    "room.expandProjection" to "true"
                )
            }
        }

        manifestPlaceholders["appIcon"] = "@mipmap/ic_launcher"
        manifestPlaceholders["appIconRound"] = "@mipmap/ic_launcher_round"
    }

    buildTypes {
        debug {
            isDebuggable = true
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
            manifestPlaceholders["appName"] = "@string/app_name_debug"
            manifestPlaceholders["appIcon"] = "@mipmap/ic_launcher_debug"
            manifestPlaceholders["appIconRound"] = "@mipmap/ic_launcher_debug_round"
        }
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            manifestPlaceholders["appName"] = "@string/app_name"
            manifestPlaceholders["appIcon"] = "@mipmap/ic_launcher"
            manifestPlaceholders["appIconRound"] = "@mipmap/ic_launcher_round"
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
        isCoreLibraryDesugaringEnabled = true
    }
    buildToolsVersion = "35.0.0"

    sourceSets {
        getByName("main") {
            manifest.srcFile("src/main/AndroidManifest.xml")
            java {
                srcDirs(listOf("src/main/java"))
            }
            res.srcDirs(listOf("src/main/res"))
        }
        getByName("androidTest") {
            res.srcDirs(listOf("src/androidTest/res"))
            java {
                srcDirs(listOf("src/androidTest/java"))
            }
        }
        getByName("test") {
            java {
                srcDirs(listOf("src/test/java"))
            }
        }
    }

    buildFeatures {
        buildConfig = true
    }

    lint {
        disable += listOf(
            "Deprecation",
            "DeprecatedApi",
            "IncorrectPackageStatement",
            "PackageLocation",
            "WrongFilePath",
            "IncorrectPath",
            "WrongPackageLocation",
            "WrongPackageName",
            "PackageName",
            "WrongFolder",
            "WrongManifestPackage",
            "PackageNameMatches",
            "InvalidPackage",
            "WrongPackage",
            "IncorrectPackageDeclaration",
            "MissingPackageDeclaration",
            "WrongPackagePrivate",
            "ExpectedPackagePrivate",
            "SourcePath",
            "PackageLocation",
            "WrongPackageLocation",
            "IncorrectPackageStatement",
            "PackageNameLocation",
            "WrongFilePath",
            "IncorrectPath",
            "ExpectedPackage",
            "WrongPackagePath",
            "WrongPackageRoot",
            "IncorrectPackageRoot",
            "WrongPackageBase",
            "IncorrectPackageBase",
            "WrongPackagePrefix",
            "IncorrectPackagePrefix",
            "WrongPackagePath",
            "IncorrectPackagePath",
            "WrongPackageRoot",
            "IncorrectPackageRoot",
            "WrongPackageBase",
            "IncorrectPackageBase",
            "WrongPackageLocation",
            "IncorrectPackageLocation",
            "WrongPackageStatement",
            "IncorrectPackageStatement",
            "WrongPackageName",
            "IncorrectPackageName",
            "PackageNameMismatch",
            "PackagePathMismatch",
            "WrongPackageDeclaration",
            "ExpectedPackageDeclaration",
            "IncorrectPackageDeclaration"
        )
        baseline = file("lint-baseline.xml")
        checkReleaseBuilds = false
        abortOnError = false
        
        lintConfig = file("lint.xml")
    }

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }
}

tasks.withType<JavaCompile> {
    options.compilerArgs.addAll(listOf(
        "-Xlint:deprecation",
        "-Xlint:unchecked"
    ))
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation("androidx.cardview:cardview:1.0.0")
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation("androidx.lifecycle:lifecycle-viewmodel:2.7.0")
    implementation("androidx.lifecycle:lifecycle-livedata:2.7.0")
    implementation("androidx.fragment:fragment:1.6.2")

    val room_version = "2.6.1"
    implementation("androidx.room:room-runtime:$room_version")
    annotationProcessor("androidx.room:room-compiler:$room_version")
    implementation("androidx.room:room-ktx:$room_version")

    val lifecycle_version = "2.7.0"
    implementation("androidx.lifecycle:lifecycle-runtime:$lifecycle_version")
    implementation("androidx.lifecycle:lifecycle-viewmodel:$lifecycle_version")
    implementation("androidx.lifecycle:lifecycle-livedata:$lifecycle_version")
    implementation("androidx.lifecycle:lifecycle-common-java8:$lifecycle_version")

    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")

    implementation("com.google.android.material:material:1.11.0")
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.0.4")

    implementation("androidx.annotation:annotation:1.7.1")
    implementation("androidx.core:core:1.12.0")
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.activity:activity:1.8.2")

    // ViewPager2
    implementation("androidx.viewpager2:viewpager2:1.0.0")
    implementation("androidx.recyclerview:recyclerview:1.3.2")

    androidTestImplementation("androidx.test:runner:1.5.2")
    androidTestImplementation("androidx.test:rules:1.5.0")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")

    // ViewModel
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.2")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.6.2")
    implementation("androidx.fragment:fragment-ktx:1.6.2")
    
    // Material Design
    implementation("com.google.android.material:material:1.10.0")

    // GridLayout
    implementation("androidx.gridlayout:gridlayout:1.0.0")

    // MPAndroidChart for charts
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")

    // Gson for JSON
    implementation("com.google.code.gson:gson:2.10.1")

    // Navigation Component
    implementation("androidx.navigation:navigation-fragment:2.7.6")
    implementation("androidx.navigation:navigation-ui:2.7.6")

    implementation("androidx.preference:preference:1.2.1")
}

idea {
    module {
        isDownloadSources = true
        isDownloadJavadoc = true
        excludeDirs = excludeDirs + file("build") + file(".gradle")
    }
}