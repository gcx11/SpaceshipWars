import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpack

plugins {
    kotlin("multiplatform") version "1.4.0"
    application
}

group = "me.gcx11"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    jcenter()
    maven {
        url = uri("https://dl.bintray.com/kotlin/ktor")
    }
    maven {
        url = uri("https://dl.bintray.com/kotlin/kotlinx")
    }
}
kotlin {
    jvm {
        compilations.all {
            kotlinOptions.jvmTarget = "1.8"
        }
        withJava()
    }
    js {
        browser {
            binaries.executable()
            webpackTask {
                cssSupport.enabled = true
            }
            runTask {
                cssSupport.enabled = true
            }
            testTask {
                useKarma {
                    useChromeHeadless()
                    webpackConfig.cssSupport.enabled = true
                }
            }
        }
    }
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-common:1.3.8")
                implementation("org.jetbrains.kotlinx:kotlinx-io:0.1.16")
                // implementation("org.jetbrains.kotlinx:atomicfu-common:0.14.1")
                implementation("io.github.microutils:kotlin-logging-common:1.7.8")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
            }
        }
        val jvmMain by getting {
            dependencies {
                implementation("io.ktor:ktor-server-netty:1.4.0")
                implementation("io.ktor:ktor-html-builder:1.4.0")
                implementation("io.ktor:ktor-websockets:1.4.0")
                implementation("org.jetbrains.kotlinx:kotlinx-io-jvm:0.1.16")
                implementation("org.jetbrains.kotlinx:kotlinx-html-jvm:0.7.2")
                implementation("ch.qos.logback:logback-classic:1.2.3")
                implementation("io.github.microutils:kotlin-logging:1.8.3")
            }
        }
        val jvmTest by getting {
            dependencies {
                implementation(kotlin("test-junit"))
            }
        }
        val jsMain by getting {
            dependencies {
                implementation("io.ktor:ktor-client-js:1.4.0")
                implementation("io.ktor:ktor-client-websockets:1.4.0")
                implementation("org.jetbrains.kotlinx:kotlinx-html-js:0.7.2")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-js:1.3.8")
                implementation("org.jetbrains.kotlinx:kotlinx-io-js:0.1.16")
                implementation("io.github.microutils:kotlin-logging-js:1.8.3")
            }
        }
        val jsTest by getting {
            dependencies {
                implementation(kotlin("test-js"))
            }
        }
    }
}

application {
    mainClassName = "me.gcx11.spaceshipwars.ServerKt"
}

tasks {
    getByName<KotlinWebpack>("jsBrowserDevelopmentWebpack") {
        outputFileName = "SpaceshipWars.js"
    }

    getByName<KotlinWebpack>("jsBrowserProductionWebpack") {
        outputFileName = "SpaceshipWars.js"
    }

    register("jvmDevelopmentProcessResources") {
        val jvmProcessResources = getByName<ProcessResources>("jvmProcessResources")
        val jsDevelopmentProductionWebpack = getByName<KotlinWebpack>("jsBrowserDevelopmentWebpack")

        this.mustRunAfter(jvmProcessResources)
        dependsOn(jsDevelopmentProductionWebpack)
        jvmProcessResources.from(File(jsDevelopmentProductionWebpack.destinationDirectory, jsDevelopmentProductionWebpack.outputFileName))
    }

    register("jvmProductionProcessResources") {
        val jvmProcessResources = getByName<ProcessResources>("jvmProcessResources")
        val jsBrowserProductionWebpack = getByName<KotlinWebpack>("jsBrowserProductionWebpack")

        this.mustRunAfter(jvmProcessResources)
        dependsOn(jsBrowserProductionWebpack)
        jvmProcessResources.from(File(jsBrowserProductionWebpack.destinationDirectory, jsBrowserProductionWebpack.outputFileName))
    }

    register<JavaExec>("runDev") {
        group = "application"
        main = "me.gcx11.spaceshipwars.ServerKt"
        args = listOf()

        val jvmJar = getByName<Jar>("jvmJar")
        val jvmDevelopmentProcessResources = getByName("jvmDevelopmentProcessResources")
        dependsOn(jvmJar, jvmDevelopmentProcessResources)
        classpath(kotlin.jvm().compilations["main"].runtimeDependencyFiles, jvmJar)
    }

    register<JavaExec>("runProd") {
        group = "application"
        main = "me.gcx11.spaceshipwars.ServerKt"
        args = listOf()

        val jvmJar = getByName<Jar>("jvmJar")
        val jvmProductionProcessResources = getByName("jvmProductionProcessResources")
        dependsOn(jvmJar, jvmProductionProcessResources)
        classpath(kotlin.jvm().compilations["main"].runtimeDependencyFiles, jvmJar)
    }
}