repositories {
    mavenCentral()
    google()
}

plugins {
    kotlin("jvm")
}

sourceSets {
    main {
        java.srcDirs("src/main/kotlin")
    }
}

val originalReleaseNotePath = "ReleaseNote.md"
val outputGitHubReleaseNotePath = "GitHubReleaseNote.md"

tasks.register("GenerateGithubReleaseNote", JavaExec::class.java) {
    args = listOf(
        rootProject.projectDir.absolutePath,
        originalReleaseNotePath,
        outputGitHubReleaseNotePath
    )

    classpath = sourceSets.named("main").get().runtimeClasspath
    mainClass.set("util.phonograph.MainGenerateGithubReleaseNoteKt")

    dependsOn(tasks.findByPath("build"))
}

tasks.register("GenerateHTML", JavaExec::class.java) {
    args = listOf(
        rootProject.projectDir.absolutePath,
        originalReleaseNotePath,
    )

    classpath = sourceSets.named("main").get().runtimeClasspath
    mainClass.set("util.phonograph.MainGenerateHtmlKt")

    dependsOn(tasks.findByPath("build"))
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}