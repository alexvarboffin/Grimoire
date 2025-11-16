rootProject.name = "Grimoire"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
rootProject.buildFileName = "build.gradle.kts"

//buildCache {
//    local {
//        isEnabled = true
//        //directory = File("Z:/gradle/build-cache")
//        directory = File(rootDir, "build-cache")
//
//    }
//}

//pluginManagement {
//    repositories {
//        google {
//            mavenContent {
//                includeGroupAndSubgroups("androidx")
//                includeGroupAndSubgroups("com.android")
//                includeGroupAndSubgroups("com.google")
//            }
//        }
//        mavenCentral()
//        gradlePluginPortal()
//
//        jcenter()
//    }
//}
//
//dependencyResolutionManagement {
//    repositories {
//        google {
//            mavenContent {
//                includeGroupAndSubgroups("androidx")
//                includeGroupAndSubgroups("com.android")
//                includeGroupAndSubgroups("com.google")
//            }
//        }
//
//        mavenCentral()
//
//        jcenter()
//    }
//}
pluginManagement {


    repositories {
        mavenLocal()
        mavenCentral()  // Primary repository for dependencies
        google()        // Required for Android-specific dependencies
        gradlePluginPortal()  // Access to Gradle plugins

//        google {
//            mavenContent {
//                includeGroupAndSubgroups("androidx")
//                includeGroupAndSubgroups("com.android")
//                includeGroupAndSubgroups("com.google")
//            }
//        }
        maven("https://maven.google.com")
        maven("https://dl.bintray.com/videolan/Android")

//        google {
//            content {
//                includeGroupByRegex("com\\.android.*")
//                includeGroupByRegex("com\\.google.*")
//                includeGroupByRegex("androidx.*")
//            }
//        }

//        mavenCentral{
//            content {
//                // Исключаем артефакты, которые должны приходить из Google
//                excludeGroupByRegex("com\\.android.*")
//                excludeGroupByRegex("com\\.google.*")
//                excludeGroupByRegex("androidx.*")
//            }
//        }

    }
}


dependencyResolutionManagement {
//    //repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
//    repositories {
//        google()
//        mavenCentral()
//    }
    //repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenLocal()
        google()

        mavenCentral()
        maven("https://jitpack.io")

    }
}

include(":composeApp")
//include(":adbclient")
//include(":shared") //kmp module
include(":lib")
include(":core:network")


include(":lib2")
