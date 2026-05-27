package org.example;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.regex.*;

@Mojo(name = "list-endpoints")
public class EndpointListerMojo extends AbstractMojo {

    @Parameter(defaultValue = "${project.basedir}/src/main/java", readonly = true)
    private File sourceDir;

    @Parameter(defaultValue = "${project.build.directory}/endpoints.txt", readonly = true)
    private File outputFile;

    public void execute() throws MojoExecutionException {
        Pattern pattern = Pattern.compile(
            "addServletMappingDecoded\\(\"(/[^\"]+)\"|@WebServlet\\(\"(/[^\"]+)\"\\)"
        );

        try {
            outputFile.getParentFile().mkdirs();
            List<String> endpoints = new ArrayList<>();

            Files.walk(sourceDir.toPath())
                .filter(p -> p.toString().endsWith(".java"))
                .sorted()
                .forEach(p -> {
                    try {
                        Matcher m = pattern.matcher(Files.readString(p));
                        while (m.find()) {
                            String ep = m.group(1) != null ? m.group(1) : m.group(2);
                            endpoints.add(ep + "  (" + sourceDir.toPath().relativize(p) + ")");
                        }
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }
                });

            try (PrintWriter out = new PrintWriter(new FileWriter(outputFile))) {
                out.println("Discovered servlet endpoints:");
                endpoints.forEach(out::println);
            }
            getLog().info("Endpoints listed in: " + outputFile);
        } catch (Exception e) {
            throw new MojoExecutionException("Failed to list endpoints", e);
        }
    }
}
