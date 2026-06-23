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

val pitestClasspath by configurations.creating

dependencies {
    implementation("org.apache.tomcat.embed:tomcat-embed-core:10.1.25")
    implementation(project(":annotations"))
    annotationProcessor(project(":processor"))

    testImplementation(platform("org.junit:junit-bom:6.1.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.junit.platform:junit-platform-suite")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    testImplementation("org.mockito:mockito-core:5.14.2")
    testImplementation("org.mockito:mockito-junit-jupiter:5.14.2")
    testImplementation("org.assertj:assertj-core:3.27.3")

    pitestClasspath("org.pitest:pitest-command-line:1.25.5")
    pitestClasspath("org.pitest:pitest-junit5-plugin:1.2.3")
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

tasks.register<JavaExec>("pitestWeak") {
    group = "verification"
    description = "PIT with weak test — mutations SURVIVE (bad coverage)"
    dependsOn(tasks.named("testClasses"))
    mainClass.set("org.pitest.mutationtest.commandline.MutationCoverageReport")
    classpath = pitestClasspath + sourceSets["test"].runtimeClasspath
    doFirst {
        val reportsDir = layout.buildDirectory.dir("reports/pitest-weak").get().asFile
        reportsDir.mkdirs()
        args(
            "--targetClasses", "org.example.order.OrderService",
            "--targetTests", "org.example.order.OrderServiceMutationWeakTest",
            "--sourceDirs", "src/main/java",
            "--reportDir", reportsDir.absolutePath,
            "--outputFormats", "HTML",
            "--mutators", "CONDITIONALS_BOUNDARY,NEGATE_CONDITIONALS",
            "--failWhenNoMutations", "false"
        )
    }
}

tasks.register<JavaExec>("pitestStrong") {
    group = "verification"
    description = "PIT with strong test — mutations KILLED (good coverage)"
    dependsOn(tasks.named("testClasses"))
    mainClass.set("org.pitest.mutationtest.commandline.MutationCoverageReport")
    classpath = pitestClasspath + sourceSets["test"].runtimeClasspath
    doFirst {
        val reportsDir = layout.buildDirectory.dir("reports/pitest-strong").get().asFile
        reportsDir.mkdirs()
        args(
            "--targetClasses", "org.example.order.OrderService",
            "--targetTests", "org.example.order.OrderServiceMutationStrongTest",
            "--sourceDirs", "src/main/java",
            "--reportDir", reportsDir.absolutePath,
            "--outputFormats", "HTML",
            "--mutators", "CONDITIONALS_BOUNDARY,NEGATE_CONDITIONALS",
            "--failWhenNoMutations", "false"
        )
    }
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
