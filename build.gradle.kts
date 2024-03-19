import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.7.21"
    kotlin("plugin.spring") version "1.7.21"
    kotlin("plugin.jpa") version "1.7.21"

    id("org.springframework.boot") version "2.7.8"
    id("io.spring.dependency-management") version "1.0.11.RELEASE"
}

group = "io.klaytn.finder"

version = "1.0.0-SNAPSHOT"

allprojects {
    configurations {
        all {
            exclude("junit", "junit")
            exclude("org.slf4j", "slf4j-log4j12")
            exclude("log4j", "log4j")
            exclude("commons-logging", "commons-logging")
            exclude("org.springframework.boot", "spring-boot-starter-tomcat")

            resolutionStrategy.cacheDynamicVersionsFor(0, TimeUnit.SECONDS)
            resolutionStrategy.cacheChangingModulesFor(0, TimeUnit.SECONDS)
        }
    }

    repositories {
        mavenLocal()
        mavenCentral()
        maven("https://jitpack.io")
    }

    tasks.withType<JavaCompile> {
        sourceCompatibility = "11"
        targetCompatibility = "11"
    }

    tasks.withType<KotlinCompile> {
        kotlinOptions {
            freeCompilerArgs = listOf("-Xjsr305=strict")
            jvmTarget = "11"
        }
    }

    tasks.withType<Test> { useJUnitPlatform() }
}

subprojects {
    apply {
        plugin("kotlin")
        plugin("kotlin-spring")
        plugin("kotlin-jpa")

        plugin("org.springframework.boot")
        plugin("io.spring.dependency-management")
    }

    val kotlinVersion by extra { "1.7.21" }
    val springBootVersion by extra { "2.7.8" }
    val okhttpVersion by extra { "4.9.1" }
    val caverVersion by extra { "1.10.2" }
    val jacksonVersion by extra { "2.13.3" }
    val retrofit2Version by extra { "2.9.0" }
    val resilience4jVersion by extra { "1.7.1" }
    val googleCloudSdkVersion by extra { "1.2.3.RELEASE" }
    val junitJupiterVersion by extra { "5.8.2" }
    val junitPlatformVersion by extra { "1.8.2" }
    val mockitoVersion by extra { "4.6.1" }
    val mockitoKotlinVersion by extra { "2.2.0" }
    val openSearchVersion by extra { "1.2.4" }
    val springDocVersion by extra { "1.6.13" }
    val springCloudVersion by extra { "3.1.3" }
    val curatorVersion by extra { "5.2.0" }
    val zookeeperVersion by extra { "3.6.3" }

    dependencies {
        annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")

        implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:$kotlinVersion")
        implementation("org.jetbrains.kotlin:kotlin-reflect:$kotlinVersion")

        implementation("org.springframework.boot:spring-boot-starter:$springBootVersion")
        implementation("org.springframework.boot:spring-boot-starter-actuator:$springBootVersion")
        implementation("org.springframework.boot:spring-boot-starter-web:$springBootVersion")
        implementation("org.springframework.boot:spring-boot-starter-jetty:$springBootVersion")
        implementation("org.springframework.boot:spring-boot-starter-websocket:$springBootVersion")
        implementation("org.springframework.boot:spring-boot-starter-data-jpa:$springBootVersion")
        implementation("org.springframework.boot:spring-boot-starter-data-redis:$springBootVersion")
        implementation("org.springframework.boot:spring-boot-starter-cache:$springBootVersion")
        implementation("org.springframework.boot:spring-boot-starter-validation:$springBootVersion")
        implementation("org.springframework.boot:spring-boot-starter-aop:$springBootVersion")
        developmentOnly("org.springframework.boot:spring-boot-devtools:$springBootVersion")

        implementation("org.springdoc:springdoc-openapi-ui:$springDocVersion")
        implementation("org.springdoc:springdoc-openapi-kotlin:$springDocVersion")
        implementation("org.springdoc:springdoc-openapi-data-rest:$springDocVersion")

        implementation(
                "org.springframework.cloud:spring-cloud-starter-zookeeper:$springCloudVersion"
        )
        implementation("org.apache.curator:curator-framework:${curatorVersion}")
        implementation("org.apache.curator:curator-recipes:${curatorVersion}")
        implementation("org.apache.zookeeper:zookeeper:${zookeeperVersion}")

        implementation("org.opensearch:opensearch:$openSearchVersion")
        implementation("org.opensearch.client:opensearch-rest-client:$openSearchVersion")
        implementation("org.opensearch.client:opensearch-rest-high-level-client:$openSearchVersion")

        api("com.fasterxml.jackson.core:jackson-core:$jacksonVersion")
        api("com.fasterxml.jackson.core:jackson-annotations:$jacksonVersion")
        api("com.fasterxml.jackson.core:jackson-databind:$jacksonVersion")
        implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:$jacksonVersion")
        api("com.fasterxml.jackson.module:jackson-module-kotlin:$jacksonVersion")
        implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:$jacksonVersion")
        implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-csv:$jacksonVersion")

        implementation("com.squareup.okhttp3:okhttp:$okhttpVersion")
        implementation("com.squareup.okhttp3:logging-interceptor:$okhttpVersion")
        api("com.squareup.retrofit2:retrofit:$retrofit2Version")
        api("com.squareup.retrofit2:converter-jackson:$retrofit2Version")
        api("io.github.resilience4j:resilience4j-circuitbreaker:$resilience4jVersion")
        api("io.github.resilience4j:resilience4j-retrofit:$resilience4jVersion")

        implementation("com.klaytn.caver:core:$caverVersion")
        implementation("net.logstash.logback:logstash-logback-encoder:6.6")

        runtimeOnly("mysql:mysql-connector-java:8.0.29")
        implementation("org.apache.commons:commons-pool2:2.11.1")
        implementation("org.apache.commons:commons-collections4:4.4")
        implementation("commons-codec:commons-codec:1.15")
        implementation("commons-io:commons-io:2.11.0")
        implementation("dev.akkinoc.util:yaml-resource-bundle:2.7.3")
        implementation("com.github.ben-manes.caffeine:caffeine:3.1.1")
        api("com.nimbusds:nimbus-jose-jwt:9.23")

        // gcp
        api("org.springframework.cloud:spring-cloud-gcp-starter:$googleCloudSdkVersion")
        api("org.springframework.cloud:spring-cloud-gcp-starter-sql-mysql:$googleCloudSdkVersion")
        api("org.springframework.cloud:spring-cloud-gcp-starter-storage:$googleCloudSdkVersion")
        api("org.springframework.cloud:spring-cloud-gcp-starter-secretmanager:$googleCloudSdkVersion")



        testImplementation("org.springframework.boot:spring-boot-starter-test:$springBootVersion")
        testImplementation("org.junit.jupiter:junit-jupiter-api:$junitJupiterVersion")
        testImplementation("org.junit.jupiter:junit-jupiter-params:$junitJupiterVersion")
        testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$junitJupiterVersion")
        testRuntimeOnly("org.junit.platform:junit-platform-launcher:$junitPlatformVersion")

        testImplementation("org.hamcrest:hamcrest-all:1.3")
        testImplementation("org.mockito:mockito-inline:$mockitoVersion")
        testImplementation("org.mockito:mockito-junit-jupiter:$mockitoVersion")
        testImplementation("com.nhaarman.mockitokotlin2:mockito-kotlin:$mockitoKotlinVersion")
        testImplementation("org.springframework.security:spring-security-crypto:5.7.1")
    }
}
