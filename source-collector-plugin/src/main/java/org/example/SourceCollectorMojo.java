package org.example;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.*;
import java.nio.file.*;

@Mojo(name = "collect")
public class SourceCollectorMojo extends AbstractMojo {

    @Parameter(defaultValue = "${project.basedir}/src/main/java", readonly = true)
    private File sourceDir;

    @Parameter(defaultValue = "${project.build.directory}/all-sources.txt", readonly = true)
    private File outputFile;

    public void execute() throws MojoExecutionException {
        try {
            outputFile.getParentFile().mkdirs();
            try (PrintWriter out = new PrintWriter(new FileWriter(outputFile))) {
                Files.walk(sourceDir.toPath())
                    .filter(p -> p.toString().endsWith(".java"))
                    .sorted()
                    .forEach(p -> {
                        out.println("// === " + sourceDir.toPath().relativize(p) + " ===");
                        try {
                            out.println(Files.readString(p));
                        } catch (IOException e) {
                            throw new UncheckedIOException(e);
                        }
                    });
            }
            getLog().info("Sources collected into: " + outputFile);
        } catch (Exception e) {
            throw new MojoExecutionException("Failed to collect sources", e);
        }
    }
}
