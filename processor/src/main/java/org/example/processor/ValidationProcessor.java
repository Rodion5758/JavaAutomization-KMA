package org.example.processor;

import org.example.annotations.AutoValidated;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Set;

@SupportedAnnotationTypes("org.example.annotations.AutoValidated")
@SupportedSourceVersion(SourceVersion.RELEASE_21)
public class ValidationProcessor extends AbstractProcessor {

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        for (Element element : roundEnv.getElementsAnnotatedWith(AutoValidated.class)) {
            if (element.getKind() != ElementKind.CLASS) continue;
            generateValidator((TypeElement) element);
        }
        return true;
    }

    private void generateValidator(TypeElement classElement) {
        String className = classElement.getSimpleName().toString();
        String packageName = processingEnv.getElementUtils()
                .getPackageOf(classElement).getQualifiedName().toString();
        String validatorName = className + "Validator";
        String qualifiedClassName = classElement.getQualifiedName().toString();

        try {
            JavaFileObject file = processingEnv.getFiler()
                    .createSourceFile(packageName + "." + validatorName, classElement);

            try (PrintWriter out = new PrintWriter(file.openWriter())) {
                out.println("package " + packageName + ";");
                out.println();
                out.println("import org.example.annotations.ValidatedBy;");
                out.println("import java.util.List;");
                out.println();
                // @ValidatedBy is a RUNTIME annotation placed on the generated class by the processor
                out.println("@ValidatedBy(target = " + qualifiedClassName + ".class)");
                out.println("public class " + validatorName + " {");
                out.println();
                out.println("    public List<String> validate(" + qualifiedClassName + " form) {");
                out.println("        return org.example.validation.ValidationEngine.validate(form);");
                out.println("    }");
                out.println("}");
            }

            processingEnv.getMessager().printMessage(
                    Diagnostic.Kind.NOTE,
                    "Generated " + validatorName + " for " + qualifiedClassName
            );
        } catch (IOException e) {
            processingEnv.getMessager().printMessage(
                    Diagnostic.Kind.ERROR,
                    "Failed to generate " + validatorName + ": " + e.getMessage()
            );
        }
    }
}
