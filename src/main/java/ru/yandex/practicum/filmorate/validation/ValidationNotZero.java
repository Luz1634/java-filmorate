package ru.yandex.practicum.filmorate.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class ValidationNotZero implements ConstraintValidator<NotZero, Integer> {
    @Override
    public void initialize(NotZero constraintAnnotation) {
    }

    @Override
    public boolean isValid(Integer number, ConstraintValidatorContext context) {
        return number == null || number != 0;
    }
}
