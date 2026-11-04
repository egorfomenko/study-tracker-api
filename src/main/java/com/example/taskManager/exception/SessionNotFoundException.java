package com.example.taskManager.exception;

public class SessionNotFoundException extends RuntimeException {
    public SessionNotFoundException(String message) {
        super(message);
    }
}
