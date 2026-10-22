package com.caique.adventOfSpring;

import jakarta.validation.Constraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.Payload;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.time.Duration;

@Constraint(validatedBy = MaxDurationValidator.class)
@Retention(RetentionPolicy.RUNTIME)
public @interface MaxDuration {

    String message() default "The Duration should be less than or equal to {max}";
    long max();
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}