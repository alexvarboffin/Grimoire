rootProject.name = "Grimoire"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

buildCache {
    local {
        isEnabled = true
        directory = File("C:/gradle-cache")
    }
}

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
        gradlePluginPortal()
        google()
        mavenLocal()
        mavenCentral()

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

        google()
        mavenLocal()
        mavenCentral()
        maven("https://jitpack.io")

    }
}

include(":composeApp")
include(":adbclient")
//include(":shared") //kmp module
include(":lib")

