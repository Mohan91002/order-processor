package com.learning.orderprocessor.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// Demonstrates: custom Bean Validation constraint (annotation + validator)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = PositiveQuantity.Validator.class)
public @interface PositiveQuantity {

    String message() default "quantity must be between 1 and 1000";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    class Validator implements ConstraintValidator<PositiveQuantity, Integer> {
        @Override
        public boolean isValid(Integer value, ConstraintValidatorContext context) {
            return value != null && value >= 1 && value <= 1000;
        }
    }
}
