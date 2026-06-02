import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.util.regex.Pattern

abstract class ListEndpointsTask : DefaultTask() {
    @TaskAction
    fun run() {
        val sourceDir = project.file("src/main/java")
        val outputFile = project.file("build/endpoints.txt")
        val pattern = Pattern.compile(
            """addServletMappingDecoded\("(/[^"]+)"|@WebServlet\("(/[^"]+)"\)"""
        )

        outputFile.parentFile.mkdirs()
        val endpoints = mutableListOf<String>()

        sourceDir.walkTopDown()
            .filter { it.extension == "java" }
            .sorted()
            .forEach { file ->
                val m = pattern.matcher(file.readText())
                while (m.find()) {
                    val ep = m.group(1) ?: m.group(2)
                    endpoints += "$ep  (${sourceDir.toPath().relativize(file.toPath())})"
                }
            }

        outputFile.printWriter().use { out ->
            out.println("Discovered servlet endpoints:")
            endpoints.forEach(out::println)
        }
        logger.lifecycle("Endpoints listed in: ${outputFile.relativeTo(project.projectDir)}")
    }
}

abstract class CollectSourcesTask : DefaultTask() {
    @TaskAction
    fun run() {
        val sourceDir = project.file("src/main/java")
        val outputFile = project.file("build/all-sources.txt")

        outputFile.parentFile.mkdirs()
        outputFile.printWriter().use { out ->
            sourceDir.walkTopDown()
                .filter { it.extension == "java" }
                .sorted()
                .forEach { file ->
                    out.println("// === ${sourceDir.toPath().relativize(file.toPath())} ===")
                    out.println(file.readText())
                }
        }
        logger.lifecycle("Sources collected into: ${outputFile.relativeTo(project.projectDir)}")
    }
}

class ServletToolsPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.tasks.register("listEndpoints", ListEndpointsTask::class.java) {
            group = "servlet tools"
            description = "Scans sources for servlet URL mappings and writes them to build/endpoints.txt"
        }
        project.tasks.register("collectSources", CollectSourcesTask::class.java) {
            group = "servlet tools"
            description = "Concatenates all .java sources into build/all-sources.txt"
        }
    }
}
