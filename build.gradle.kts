import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    java
    id("com.gradleup.shadow") version "9.0.0"
}

apply<ServletToolsPlugin>()

allprojects {
    repositories {
        mavenCentral()
    }

    plugins.withType<JavaPlugin> {
        apply(plugin = "checkstyle")
        extensions.configure<org.gradle.api.plugins.quality.CheckstyleExtension> {
            toolVersion = "10.23.0"
            configFile = rootProject.file("config/checkstyle/checkstyle.xml")
            maxWarnings = 0
            isIgnoreFailures = false
        }
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.apache.tomcat.embed:tomcat-embed-core:10.1.25")
    implementation(project(":annotations"))
    annotationProcessor(project(":processor"))

    testImplementation(platform("org.junit:junit-bom:6.1.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.junit.platform:junit-platform-suite")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.named<ShadowJar>("shadowJar") {
    mergeServiceFiles()
    archiveClassifier.set("all")
    manifest {
        attributes["Main-Class"] = "org.example.Main"
    }
}

tasks.test {
    useJUnitPlatform()
    testLogging { events("passed", "skipped", "failed") }
}

tasks.register<Test>("testFast") {
    group = "verification"
    description = "Runs only tests tagged 'fast' (FastSuite)"
    testClassesDirs = sourceSets["test"].output.classesDirs
    classpath = sourceSets["test"].runtimeClasspath
    filter { excludeTestsMatching("*.suite.*") }
    useJUnitPlatform { includeTags("fast") }
    testLogging { events("passed", "skipped", "failed") }
}

tasks.register<Test>("testSlow") {
    group = "verification"
    description = "Runs only tests tagged 'slow' (SlowSuite)"
    testClassesDirs = sourceSets["test"].output.classesDirs
    classpath = sourceSets["test"].runtimeClasspath
    filter { excludeTestsMatching("*.suite.*") }
    useJUnitPlatform { includeTags("slow") }
    testLogging { events("passed", "skipped", "failed") }
}

tasks.register("countServlets") {
    group = "servlet tools"
    description = "Counts the number of servlets registered in Main.java"
    doLast {
        val main = file("src/main/java/org/example/Main.java")
        val count = main.readText().split("addServlet(").size - 1
        println("Registered servlets: $count")
    }
}
