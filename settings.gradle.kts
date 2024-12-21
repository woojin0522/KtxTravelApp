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
        maven(url = "https://repository.map.naver.com/archive/maven")
        maven(url = "https://jitpack.io")
    }
}

rootProject.name = "KtxTravelApplication"
include(":app")
