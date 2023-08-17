apply {
    plugin("kotlin-spring")
    plugin("kotlin-jpa")
}

group = "io.klaytn.finder"

version = "1.0.0-SNAPSHOT"

dependencies { implementation(project(":module-common")) }

tasks.jar {
    archiveBaseName.set("finder-domain")
    archiveVersion.set("1.0.0-SNAPSHOT")
}
