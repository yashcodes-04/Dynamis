-- Date: 2026-11-16 - Yash
-- Schema for Gym Website Authentication System
CREATE DATABASE IF NOT EXISTS gymdb;
USE gymdb;

CREATE TABLE IF NOT EXISTS users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    -- Roll Number format: Year + (1000 + auto_increment id), e.g. 20261001, 20261002
    gym_id VARCHAR(20) UNIQUE,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    phone VARCHAR(20),
    password_hash VARCHAR(255) NOT NULL,
    role ENUM('MEMBER', 'RECEPTIONIST', 'TRAINER', 'OWNER') NOT NULL,
    membership_plan VARCHAR(50) DEFAULT NULL,
    membership_price DECIMAL(10, 2) DEFAULT 0.00,
    membership_start_date DATE DEFAULT NULL,
    membership_end_date DATE DEFAULT NULL,
    membership_duration_days INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Fee Payments & Membership Renewal History
CREATE TABLE IF NOT EXISTS fee_payments (
    id INT AUTO_INCREMENT PRIMARY KEY,
    gym_id VARCHAR(20),
    member_email VARCHAR(100) NOT NULL,
    member_name VARCHAR(100) NOT NULL,
    plan_type VARCHAR(50) NOT NULL,
    amount DECIMAL(10, 2) NOT NULL,
    payment_mode VARCHAR(50) NOT NULL,
    payment_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    renewal_due_date DATE NOT NULL
);

-- Member Attendance & Check-In Log
CREATE TABLE IF NOT EXISTS attendance (
    id INT AUTO_INCREMENT PRIMARY KEY,
    gym_id VARCHAR(20) NOT NULL,
    member_name VARCHAR(100) NOT NULL,
    member_email VARCHAR(100) NOT NULL,
    check_in_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Member Booked Fitness Classes & Sessions
CREATE TABLE IF NOT EXISTS booked_sessions (
    id INT AUTO_INCREMENT PRIMARY KEY,
    gym_id VARCHAR(20),
    member_email VARCHAR(100) NOT NULL,
    member_name VARCHAR(100) NOT NULL,
    class_name VARCHAR(100) NOT NULL,
    description VARCHAR(255),
    level VARCHAR(50),
    duration VARCHAR(50) NOT NULL,
    price DECIMAL(10, 2) NOT NULL,
    schedule_slot VARCHAR(100),
    status VARCHAR(50) DEFAULT 'CONFIRMED',
    booking_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Optional: Initial Seed Users for testing each role if needed (passwords: 'admin123' hashed)
-- You can register them via the UI or run SQL seeds.
