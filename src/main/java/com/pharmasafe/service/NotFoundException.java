package com.pharmasafe.service;

/** Thrown when a requested medicine, batch or recall does not exist. */
public class NotFoundException extends RuntimeException {
    public NotFoundException(String message) { super(message); }
}
