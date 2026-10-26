// Flatpak builds have no network: plugins and dependencies are taken from ./offline-repository,
// populated by flatpak-builder from gradle-sources-*.json (./gradlew flatpakGradleGenerator --no-configuration-cache).
settingsEvaluated {
    val offlineRepository = settings.rootDir.resolve("offline-repository").toURI()
    pluginManagement.repositories {
        clear()
        maven { url = offlineRepository }
    }
    dependencyResolutionManagement.repositories {
        clear()
        maven { url = offlineRepository }
    }
}
