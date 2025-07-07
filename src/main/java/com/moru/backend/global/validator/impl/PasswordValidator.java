package com.moru.backend.global.validator.impl;

import com.moru.backend.global.validator.annotation.Password;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

public class PasswordValidator implements ConstraintValidator<Password, String> {
    private static final Pattern COMPLEX_PASSWORD_PATTERN = Pattern.compile(
            "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[~!@#$%^&*()_+\\-={}\\[\\]:\";'<>?,./])[A-Za-z\\d~!@#$%^&*()_+\\-={}\\[\\]:\";'<>?,./]{8,20}$"
    );

    @Override
    public boolean isValid(String password, ConstraintValidatorContext context) {
        if(password == null) return false;

        if(!COMPLEX_PASSWORD_PATTERN.matcher(password).matches()) return false;

        return true;
    }
}
