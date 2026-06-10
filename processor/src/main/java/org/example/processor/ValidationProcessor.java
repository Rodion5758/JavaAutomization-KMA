package org.example.processor;

import org.example.annotations.AutoValidated;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.util.ElementFilter;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

@SupportedAnnotationTypes("org.example.annotations.AutoValidated")
@SupportedSourceVersion(SourceVersion.RELEASE_21)
public class ValidationProcessor extends AbstractProcessor {

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        for (Element element : roundEnv.getElementsAnnotatedWith(AutoValidated.class)) {
            if (element.getKind() != ElementKind.CLASS) continue;
            TypeElement cls = (TypeElement) element;
            String pkg = processingEnv.getElementUtils().getPackageOf(cls).getQualifiedName().toString();
            String name = cls.getSimpleName().toString();
            generateAnnotatedForm(cls, pkg, name);
            generateValidator(cls, pkg, name);
        }
        return true;
    }

    private void generateAnnotatedForm(TypeElement cls, String pkg, String name) {
        String generatedName = name + "Generated";
        List<VariableElement> fields = ElementFilter.fieldsIn(cls.getEnclosedElements());

        writeSourceFile(pkg + "." + generatedName, cls, out -> {
            out.println("package " + pkg + ";");
            out.println();
            out.println("import org.example.annotations.NotNull;");
            out.println("import org.example.annotations.MinLength;");
            out.println("import org.example.annotations.Range;");
            out.println();
            out.println("public class " + generatedName + " {");
            out.println();

            List<String> ctorParams = new ArrayList<>();
            List<String> ctorAssigns = new ArrayList<>();
            for (VariableElement f : fields) {
                writeFieldAnnotations(f, out);
                out.println("    private " + f.asType() + " " + f.getSimpleName() + ";");
                out.println();
                ctorParams.add(f.asType() + " " + f.getSimpleName());
                ctorAssigns.add("        this." + f.getSimpleName() + " = " + f.getSimpleName() + ";");
            }

            out.println("    public " + generatedName + "(" + String.join(", ", ctorParams) + ") {");
            ctorAssigns.forEach(out::println);
            out.println("    }");
            out.println();

            for (VariableElement f : fields) {
                String n = f.getSimpleName().toString();
                out.println("    public " + f.asType() + " get" + capitalize(n) + "() { return " + n + "; }");
            }

            out.println("}");
        });
    }

    private void writeFieldAnnotations(VariableElement field, PrintWriter out) {
        for (AnnotationMirror mirror : field.getAnnotationMirrors()) {
            String type = mirror.getAnnotationType().toString();
            if (type.equals("org.example.annotations.NotNull")) {
                out.println("    @NotNull");
            } else if (type.equals("org.example.annotations.MinLength")) {
                out.println("    @MinLength(" + intValue(mirror, "value") + ")");
            } else if (type.equals("org.example.annotations.Range")) {
                out.println("    @Range(min = " + intValue(mirror, "min") + ", max = " + intValue(mirror, "max") + ")");
            }
        }
    }

    private int intValue(AnnotationMirror mirror, String key) {
        for (var e : processingEnv.getElementUtils().getElementValuesWithDefaults(mirror).entrySet()) {
            if (e.getKey().getSimpleName().contentEquals(key))
                return (Integer) e.getValue().getValue();
        }
        return 0;
    }

    private void generateValidator(TypeElement cls, String pkg, String name) {
        String validatorName = name + "Validator";
        String qualifiedName = cls.getQualifiedName().toString();
        String generatedFormType = pkg + "." + name + "Generated";

        writeSourceFile(pkg + "." + validatorName, cls, out -> {
            out.println("package " + pkg + ";");
            out.println();
            out.println("import org.example.annotations.ValidatedBy;");
            out.println("import java.util.List;");
            out.println();
            out.println("@ValidatedBy(target = " + qualifiedName + ".class)");
            out.println("public class " + validatorName + " {");
            out.println();
            out.println("    public List<String> validate(" + generatedFormType + " form) {");
            out.println("        return org.example.validation.ValidationEngine.validate(form);");
            out.println("    }");
            out.println("}");
        });
    }

    private void writeSourceFile(String qualifiedName, TypeElement origin, Consumer<PrintWriter> writer) {
        try {
            JavaFileObject file = processingEnv.getFiler().createSourceFile(qualifiedName, origin);
            try (PrintWriter out = new PrintWriter(file.openWriter())) {
                writer.accept(out);
            }
            processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "Generated " + qualifiedName);
        } catch (IOException e) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
                    "Failed to generate " + qualifiedName + ": " + e.getMessage());
        }
    }

    private static String capitalize(String s) {
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }
}
