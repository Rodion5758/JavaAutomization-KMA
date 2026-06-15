package org.example.validation;

import org.example.annotations.NotNull;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

public class ValidationEngineTest {

    @BeforeEach
    void requireRuntimeAnnotations() throws NoSuchFieldException {
        assumeTrue(
            UserRegistrationForm.class.getDeclaredField("email").isAnnotationPresent(NotNull.class),
            "Skipping: @NotNull on 'email' is not RUNTIME-retained — validation results would be meaningless"
        );
    }

    @Test
    @Tag("fast")
    void validFormHasNoErrors() {
        UserRegistrationForm form = new UserRegistrationForm("alice", "alice@example.com", 25);
        assertTrue(ValidationEngine.validate(form).isEmpty());
    }

    @ParameterizedTest
    @Tag("fast")
    @ValueSource(strings = {"a", "", " "})
    void shortOrBlankUsernameProducesError(String username) {
        UserRegistrationForm form = new UserRegistrationForm(username, "x@y.com", 30);
        List<String> errors = ValidationEngine.validate(form);
        assertTrue(errors.stream().anyMatch(e -> e.startsWith("username")));
    }

    static Stream<Arguments> formCases() {
        return Stream.of(
            Arguments.of("alice", "alice@example.com", 25, 0),
            Arguments.of("b",     "b@b.com",           20, 1),
            Arguments.of("carol", null,                 40, 1),
            Arguments.of("d",     null,                -5, 3)
        );
    }

    @ParameterizedTest
    @Tag("fast")
    @MethodSource("formCases")
    void formErrorCount(String username, String email, int age, int expectedErrors) {
        UserRegistrationForm form = new UserRegistrationForm(username, email, age);
        assertEquals(expectedErrors, ValidationEngine.validate(form).size());
    }

    @TestFactory
    @Tag("slow")
    Stream<DynamicTest> ageBoundaryTests() {
        record Case(int age, boolean valid) {}
        return Stream.of(
            new Case(-1,  false),
            new Case(0,   true),
            new Case(60,  true),
            new Case(120, true),
            new Case(121, false)
        ).map(c -> dynamicTest("age=" + c.age(), () -> {
            UserRegistrationForm form = new UserRegistrationForm("alice", "alice@example.com", c.age());
            List<String> errors = ValidationEngine.validate(form);
            long ageErrors = errors.stream().filter(e -> e.startsWith("age")).count();
            if (c.valid()) assertEquals(0, ageErrors);
            else           assertEquals(1, ageErrors);
        }));
    }
}
