plugins {
    java
}

apply<ServletToolsPlugin>()

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
