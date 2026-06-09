package org.example.validation;

import org.example.annotations.ValidatedBy;

import java.util.List;

public class ValidationDemo {

    public static void main(String[] args) {
        UserRegistrationFormValidator validator = new UserRegistrationFormValidator();

        // Read @ValidatedBy from the generated class at runtime
        ValidatedBy meta = validator.getClass().getAnnotation(ValidatedBy.class);
        System.out.println("=== Validator targets: " + meta.target().getSimpleName() + " ===");
        System.out.println();

        // Invalid form: username too short, email null, age out of range
        UserRegistrationForm bad = new UserRegistrationForm("a", null, 200);
        List<String> errors = validator.validate(bad);
        System.out.println("Invalid form errors:");
        errors.forEach(e -> System.out.println("  - " + e));

        System.out.println();

        // Valid form
        UserRegistrationForm good = new UserRegistrationForm("alice", "alice@example.com", 25);
        List<String> ok = validator.validate(good);
        System.out.println("Valid form errors: " + (ok.isEmpty() ? "none — passed!" : ok));
    }
}
