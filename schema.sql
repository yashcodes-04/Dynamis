-- Date: 2026-11-16 - Yash
-- Schema for Gym Website Authentication System
CREATE DATABASE IF NOT EXISTS gymdb;
USE gymdb;

CREATE TABLE IF NOT EXISTS users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    phone VARCHAR(20),
    password_hash VARCHAR(255) NOT NULL,
    role ENUM('MEMBER', 'RECEPTIONIST', 'TRAINER', 'OWNER') NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Fee Payments & Membership Renewal History
CREATE TABLE IF NOT EXISTS fee_payments (
    id INT AUTO_INCREMENT PRIMARY KEY,
    member_email VARCHAR(100) NOT NULL,
    member_name VARCHAR(100) NOT NULL,
    plan_type VARCHAR(50) NOT NULL,
    amount DECIMAL(10, 2) NOT NULL,
    payment_mode VARCHAR(50) NOT NULL,
    payment_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    renewal_due_date DATE NOT NULL
);

-- Optional: Initial Seed Users for testing each role if needed (passwords: 'admin123' hashed)
-- You can register them via the UI or run SQL seeds.
