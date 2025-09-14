package ru.skillbox.exceptions;

public class InvalidPhoneFormatException extends RuntimeException {
    public InvalidPhoneFormatException(String message) {
        super(message);
    }
}