package com.moru.backend.domain.auth.util;

public class PasswordValidator {
    private static final String PASSWORD_PATTERN =
            "^(?=.*\\d)(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].{8,}$)";
    public static boolean validatePassword(String password) {
        return password != null && password.matches(PASSWORD_PATTERN);
    }
}
