package com.takehome.stayease.exception;

public class RoomUnavailableException extends RuntimeException{
    public RoomUnavailableException(String message) {
        super(message);
    }

    public RoomUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
