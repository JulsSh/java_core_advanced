package ru.skillbox.exceptions;

public class InvalidComponentCountException extends RuntimeException {
    public InvalidComponentCountException(String message) {
        super(message);
    }
}
