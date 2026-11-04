package com.example.taskManager.exception;

public class TagNotFoundException extends RuntimeException {
    public TagNotFoundException(String message) {
        super(message);
    }
}
