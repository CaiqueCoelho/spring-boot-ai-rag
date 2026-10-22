package com.caique.adventOfSpring;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.Duration;

public class MaxDurationValidator implements ConstraintValidator<MaxDuration, Duration> {

    private long maxSeconds;


    @Override
    public void initialize(MaxDuration constraintAnnotation) {
        this.maxSeconds = constraintAnnotation.max();
    }

    @Override
    public boolean isValid(Duration value, ConstraintValidatorContext context) {
        if(value == null){
            return false;
        }

        return value.getSeconds() <= maxSeconds;
    }
}
