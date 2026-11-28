package com.example.taskManager.exception;

import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionsHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail invalidFields(MethodArgumentNotValidException error) {
        var fields = error.getBindingResult().getFieldErrors().stream()
                .map(field -> field.getField()).distinct().toList();
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST,
                "Проверь поля: " + String.join(", ", fields));
    }


    @ExceptionHandler(TaskNotFoundException.class)
    public ProblemDetail taskNotFoundException(){
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.NOT_FOUND,
                "Задача с таким id не найдена"
        );
        problem.setTitle("Задача не найдена");

        return problem;
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ProblemDetail userNotFoundException(){
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.NOT_FOUND,
                "Пользователь с таким id не найден"
        );
        problemDetail.setTitle("Пользователь не найден");

        return problemDetail;
    }

    @ExceptionHandler(SessionNotFoundException.class)
    public ProblemDetail sessionNotFoundException(){
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.NOT_FOUND,
                "Сессия с таким id не найдена"
        );
        problemDetail.setTitle("Сессия не найдена");

        return problemDetail;
    }

    @ExceptionHandler(TagNotFoundException.class)
    public ProblemDetail tagNotFoundException(){
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.NOT_FOUND,
                "Тег с таким id не найден"
        );
        problemDetail.setTitle("Тег не найден");

        return problemDetail;
    }

    @ExceptionHandler(InvalidRequestException.class)
    public ProblemDetail invalidRequestException(InvalidRequestException invalidRequestException){
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                invalidRequestException.getMessage()
        );
        problemDetail.setTitle("В запросе имеются ошибки");

        return problemDetail;
    }

    @ExceptionHandler(ConflictExсeption.class)
    public ProblemDetail conflictExeption(ConflictExсeption conflictExeption){
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.CONFLICT,
                conflictExeption.getMessage()
        );
        problemDetail.setTitle("Был вызван конфликт");

        return problemDetail;
    }


}
