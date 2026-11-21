/**
 * Date: 2026-11-18 - Yash
 * Syllabus: Unit 1 (Lecture 3) - Custom Exceptions
 */
package com.gym.exception;

public class MemberNotFoundException extends GymException {
    public MemberNotFoundException(String message) {
        super(message);
    }
}
