pluginManagement {
    repositories {
        gradlePluginPortal()
    }
}

rootProject.name = "finder-api"
include("module-common")
include("module-domain")
include("module-front-api")
include("module-open-api")
include("module-worker")
include("module-compiler-api")
include("module-gateway")