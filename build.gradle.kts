import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.time.LocalDateTime.now

val kotlinxHtmlVersion = project.extra.properties["kotlinxHtmlVersion"].toString()

plugins {
    kotlin("jvm") version "1.7.21"
    application
}

group = "ru.sikuda"
val fileVersion = "projectVersion.txt"

repositories {
    mavenCentral()
}

dependencies {

    implementation("com.google.code.gson:gson:2.10")

    // include for JVM target
    implementation("org.jetbrains.kotlinx:kotlinx-html-jvm:$kotlinxHtmlVersion")

    // include for JS target
    //implementation("org.jetbrains.kotlinx:kotlinx-html-js:$kotlinxHtmlVersion")

    // include for Common module
    implementation("org.jetbrains.kotlinx:kotlinx-html:$kotlinxHtmlVersion")

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.4.1")

    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "11"
}

application {
    mainClass.set("MainKt")
    version = File(fileVersion).readText().split("=").get(1).trim()
}

tasks.register("meta-log") {

    shouldRunAfter("build")

    println("Group: ${project.group}")
    println("Version: ${version}")
    val artifactId = project.name
    println("artifactId: ${artifactId}")
    println("Time run: ${now()}")

}.get()

tasks {
    this.named("jar")
    withType<Jar> {
        manifest {
            attributes("Main-Class" to "MainKt")
        }
    }

    task<Jar>("fatJar") {
        archiveBaseName.set(project.name + "-fat-jar")
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE

        manifest {
            attributes("Main-Class" to "MainKt")
        }
        from(
            configurations.runtimeClasspath.get()
                .map { if (it.isDirectory) it else zipTree(it) }
        )
        with(named("jar").get() as CopySpec)
    }

    build {
        dependsOn("fatJar")
    }

}

tasks.register("IncreaseVersion") {

    val oldVersion = File(fileVersion).readText().split("=").get(1).trim()
    println("Old version=$oldVersion")
    val list = oldVersion.split(".")
    val rev = list[2].toInt() + 1
    val newVersion = "${list[0]}.${list[1]}.$rev"
    println("New version=$newVersion")
    File(fileVersion).writeText("version=$newVersion")

}



