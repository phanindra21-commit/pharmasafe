package com.pharmasafe.service;

/** Thrown when an action conflicts with the current state (e.g. recalling an already recalled batch). */
public class ConflictException extends RuntimeException {
    public ConflictException(String message) { super(message); }
}
