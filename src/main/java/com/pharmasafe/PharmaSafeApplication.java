package com.pharmasafe;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * PharmaSafe: an intelligence layer for pharmacists that connects
 * medicine safety signals, batch traceability, recall response and
 * inter-pharmacy availability.
 *
 * It assists the pharmacist; it does not diagnose, prescribe, or
 * independently declare a medicine authentic.
 */
@SpringBootApplication
public class PharmaSafeApplication {
    public static void main(String[] args) {
        SpringApplication.run(PharmaSafeApplication.class, args);
    }
}
