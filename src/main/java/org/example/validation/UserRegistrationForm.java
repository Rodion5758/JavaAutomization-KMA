package org.example.validation;

import org.example.annotations.AutoValidated;
import org.example.annotations.MinLength;
import org.example.annotations.NotNull;
import org.example.annotations.Range;

@AutoValidated
public class UserRegistrationForm {

    @NotNull
    @MinLength(2)
    private String username;

    @NotNull
    private String email;

    @Range(min = 0, max = 120)
    private int age;

    public UserRegistrationForm(String username, String email, int age) {
        this.username = username;
        this.email = email;
        this.age = age;
    }

    public String getUsername() { return username; }
    public String getEmail()    { return email; }
    public int    getAge()      { return age; }
}
