package org.example.validation;

import org.example.annotations.MinLength;
import org.example.annotations.NotNull;
import org.example.annotations.Range;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class ValidationEngine {

    public static List<String> validate(Object obj) {
        List<String> errors = new ArrayList<>();
        for (Field field : obj.getClass().getDeclaredFields()) {
            field.setAccessible(true);
            Object value;
            try {
                value = field.get(obj);
            } catch (IllegalAccessException e) {
                continue;
            }

            if (field.isAnnotationPresent(NotNull.class)) {
                if (value == null || (value instanceof String s && s.isBlank())) {
                    errors.add(field.getName() + ": must not be null or blank");
                }
            }

            if (field.isAnnotationPresent(MinLength.class)) {
                int min = field.getAnnotation(MinLength.class).value();
                if (value instanceof String s && s.length() < min) {
                    errors.add(field.getName() + ": must be at least " + min + " characters");
                }
            }

            if (field.isAnnotationPresent(Range.class)) {
                Range range = field.getAnnotation(Range.class);
                if (value instanceof Number n) {
                    long v = n.longValue();
                    if (v < range.min() || v > range.max()) {
                        errors.add(field.getName() + ": must be between " + range.min() + " and " + range.max());
                    }
                }
            }
        }
        return errors;
    }
}
