package ru.yandex.practicum.filmorate.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.yandex.practicum.filmorate.exception.exceptions.AddExistObjectException;
import ru.yandex.practicum.filmorate.exception.exceptions.IncorrectObjectModificationException;
import ru.yandex.practicum.filmorate.exception.exceptions.NoSuchEnumException;
import ru.yandex.practicum.filmorate.exception.exceptions.UpdateNonExistObjectException;

import javax.validation.ConstraintViolationException;
import java.util.ArrayList;
import java.util.List;

@RestControllerAdvice("ru.yandex.practicum.filmorate.controller")
@Slf4j
public class GlobalExceptionHandlerController {
    @ExceptionHandler
    public ResponseEntity<ExceptionHandlerResponse> validation(MethodArgumentNotValidException exception) {
        List<String> exceptionMessages = new ArrayList<>();

        for (FieldError fieldError : exception.getBindingResult().getFieldErrors()) {
            exceptionMessages.add(fieldError.getDefaultMessage());
        }

        ExceptionHandlerResponse response = new ExceptionHandlerResponse("Ошибка при валидации объекта",
                exceptionMessages.toString());

        log.warn(response.toString());
        return ResponseEntity.badRequest().body(response);
    }


    @ExceptionHandler
    public ResponseEntity<ExceptionHandlerResponse> invalidRequest(HttpMessageNotReadableException exception) {
        ExceptionHandlerResponse response = new ExceptionHandlerResponse("Запрос составлен неправильно",
                exception.getMessage());
        log.warn(response.toString());
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler
    public ResponseEntity<ExceptionHandlerResponse> invalidValidated(ConstraintViolationException exception) {
        ExceptionHandlerResponse response = new ExceptionHandlerResponse("Задано неправильное значение переменной",
                exception.getMessage());
        log.warn(response.toString());
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler
    public ResponseEntity<ExceptionHandlerResponse> addExistObject(AddExistObjectException exception) {
        ExceptionHandlerResponse response = new ExceptionHandlerResponse("Данный объект уже существует",
                exception.getMessage());
        log.warn(response.toString());
        return ResponseEntity.internalServerError().body(response);
    }

    @ExceptionHandler
    public ResponseEntity<ExceptionHandlerResponse> updateNonExistObject(UpdateNonExistObjectException exception) {
        ExceptionHandlerResponse response = new ExceptionHandlerResponse("Попытка доступа к несуществующему объекту",
                exception.getMessage());
        log.warn(response.toString());
        return ResponseEntity.status(404).body(response);
    }

    @ExceptionHandler
    public ResponseEntity<ExceptionHandlerResponse> incorrectObjectModification(IncorrectObjectModificationException exception) {
        ExceptionHandlerResponse response = new ExceptionHandlerResponse("Некорректное изменение объекта",
                exception.getMessage());
        log.warn(response.toString());
        return ResponseEntity.internalServerError().body(response);
    }

    @ExceptionHandler
    public ResponseEntity<ExceptionHandlerResponse> noSuchEnumException(NoSuchEnumException exception) {
        ExceptionHandlerResponse response = new ExceptionHandlerResponse("Некорректное переданное справочное значение",
                exception.getMessage());
        log.warn(response.toString());
        return ResponseEntity.internalServerError().body(response);
    }
}