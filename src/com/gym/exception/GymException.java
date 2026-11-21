/**
 * Date: 2026-11-18 - Yash
 * Syllabus: Unit 1 (Lecture 3) - Custom Exceptions & Exception Propagation
 */
package com.gym.exception;

public class GymException extends Exception {
    public GymException(String message) {
        super(message);
    }

    public GymException(String message, Throwable cause) {
        super(message, cause);
    }
}
