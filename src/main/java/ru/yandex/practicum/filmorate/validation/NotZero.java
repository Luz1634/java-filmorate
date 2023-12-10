package ru.yandex.practicum.filmorate.validation;

import javax.validation.Constraint;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ValidationNotZero.class)
public @interface NotZero {
    String message() default "id не может быть null или 0";

    Class<?>[] groups() default {};

    Class<?>[] payload() default {};
}
