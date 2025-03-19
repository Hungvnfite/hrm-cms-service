package com.example.cms.validate;


import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class BooleanValueValidator implements ConstraintValidator<BooleanValue, Boolean> {

    @Override
    public void initialize(BooleanValue constraintAnnotation) {
        // No initialization needed
    }

    @Override
    public boolean isValid(Boolean value, ConstraintValidatorContext context) {
        return value == null || value.equals(Boolean.TRUE) || value.equals(Boolean.FALSE);
    }
}