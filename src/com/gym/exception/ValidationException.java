/**
 * Date: 2026-11-18 - Yash
 * Syllabus: Unit 1 (Lecture 3, 4) - Custom Exceptions and Input Validation
 */
package com.gym.exception;

public class ValidationException extends GymException {
    public ValidationException(String message) {
        super(message);
    }
}
