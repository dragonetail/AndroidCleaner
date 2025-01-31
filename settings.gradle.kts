pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}

rootProject.name = "AndroidCleaner"
include(":app")

// 添加包名映射
gradle.beforeProject {
    extra["packageName"] = "com.blackharry.androidcleaner"
    extra["packagePath"] = "com/blackharry/androidcleaner"
    
    // 设置源代码路径
    extra["sourcePath"] = "src/main/java"
    extra["testSourcePath"] = "src/test/java"
    extra["androidTestSourcePath"] = "src/androidTest/java"
    
    // 设置包名映射
    extra["packageMapping"] = mapOf(
        "app.src.main.java.com.blackharry.androidcleaner" to "com.blackharry.androidcleaner",
        "app.src.test.java.com.blackharry.androidcleaner" to "com.blackharry.androidcleaner",
        "app.src.androidTest.java.com.blackharry.androidcleaner" to "com.blackharry.androidcleaner"
    )
}
 