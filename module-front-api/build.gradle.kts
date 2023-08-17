import org.springframework.boot.gradle.tasks.bundling.BootWar

plugins { war }

group = "io.klaytn.finder"

version = "1.0.0-SNAPSHOT"

dependencies {
    implementation(project(":module-common"))
    implementation(project(":module-domain"))
}

tasks.getByName<BootWar>("bootWar") {
    archiveBaseName.set("finder-api")
    archiveVersion.set("")
}

tasks.bootRun {
    System.getProperties().forEach { systemProperty(it.key.toString(), it.value) }

    val profile = System.getProperty("spring.profiles.active") ?: "prodCypress"
    systemProperty("spring.profiles.active", profile)
}
