pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    // Kotlin/JS and Kotlin/Wasm download their toolchains through Ivy repositories.
    // Keep dependency resolution centralized while explicitly allowing only those
    // tool distribution groups from their official upstream locations.
    repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS)
    repositories {
        google()
        mavenCentral()

        exclusiveContent {
            forRepository {
                ivy("https://nodejs.org/dist/") {
                    name = "Node.js distributions"
                    patternLayout {
                        artifact("v[revision]/[artifact](-v[revision]-[classifier]).[ext]")
                    }
                    metadataSources { artifact() }
                }
            }
            filter { includeGroup("org.nodejs") }
        }

        exclusiveContent {
            forRepository {
                ivy("https://github.com/yarnpkg/yarn/releases/download") {
                    name = "Yarn distributions"
                    patternLayout {
                        artifact("v[revision]/[artifact](-v[revision]).[ext]")
                    }
                    metadataSources { artifact() }
                }
            }
            filter { includeGroup("com.yarnpkg") }
        }

        exclusiveContent {
            forRepository {
                ivy("https://github.com/WebAssembly/binaryen/releases/download") {
                    name = "Binaryen distributions"
                    patternLayout {
                        artifact("version_[revision]/[module]-version_[revision]-[classifier].[ext]")
                    }
                    metadataSources { artifact() }
                }
            }
            filter { includeGroup("com.github.webassembly") }
        }
    }
}

rootProject.name = "rps-arena"
include(":shared", ":androidApp", ":desktopApp", ":webApp")
