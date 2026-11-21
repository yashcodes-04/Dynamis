/**
 * Date: 2026-11-18 - Yash
 * Syllabus: Unit 6 (Lectures 25, 26, 27) - 
 *           Byte streams (FileInputStream, FileOutputStream),
 *           Character streams (FileReader, FileWriter, BufferedReader, BufferedWriter),
 *           File Handling + Collections Integration: text/CSV read-write, validation,
 *           collection import/export, and exception-safe resource handling (try-with-resources).
 */
package com.gym.util;

import com.gym.exception.GymException;
import com.gym.exception.ValidationException;
import com.gym.model.AttendanceRecord;
import com.gym.model.FeePayment;

import java.io.*;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class FileExportUtil {

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    // ==========================================
    // Character Streams - Writing (Lectures 25-27)
    // ==========================================

    /**
     * Exports a List of FeePayment collection objects to a comma-separated (CSV) file.
     * Demonstrates Character Stream (FileWriter / BufferedWriter) and try-with-resources.
     */
    public static void exportPaymentsToCSV(List<FeePayment> payments, File targetFile) throws GymException {
        if (payments == null || payments.isEmpty()) {
            throw new ValidationException("Cannot export empty payment records.");
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(targetFile))) {
            // Write CSV Header
            writer.write("Payment ID,Payment Date,Gym ID,Member Name,Email,Plan,Amount (Rs.),Payment Mode,Renewal Due Date");
            writer.newLine();

            // Iterate over collection and write lines (Lecture 26/27)
            for (FeePayment fp : payments) {
                StringBuilder sb = new StringBuilder();
                sb.append(fp.getId()).append(",");
                sb.append(fp.getPaymentDate() != null ? DATE_FORMAT.format(fp.getPaymentDate()) : "N/A").append(",");
                sb.append(escapeCSV(fp.getGymId())).append(",");
                sb.append(escapeCSV(fp.getMemberName())).append(",");
                sb.append(escapeCSV(fp.getMemberEmail())).append(",");
                sb.append(escapeCSV(fp.getPlanType())).append(",");
                sb.append(String.format("%.2f", fp.getAmount())).append(",");
                sb.append(escapeCSV(fp.getPaymentMode())).append(",");
                sb.append(fp.getRenewalDueDate() != null ? fp.getRenewalDueDate().toString() : "N/A");

                writer.write(sb.toString());
                writer.newLine();
            }
        } catch (IOException e) {
            throw new GymException("Failed to export payment records to CSV: " + e.getMessage(), e);
        }
    }

    /**
     * Exports a List of AttendanceRecord collection objects to a comma-separated (CSV) file.
     */
    public static void exportAttendanceToCSV(List<AttendanceRecord> records, File targetFile) throws GymException {
        if (records == null || records.isEmpty()) {
            throw new ValidationException("Cannot export empty attendance records.");
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(targetFile))) {
            writer.write("Entry ID,Check-In Timestamp,Gym ID,Member Name,Email Address,Status");
            writer.newLine();

            for (AttendanceRecord ar : records) {
                StringBuilder sb = new StringBuilder();
                sb.append(ar.getId()).append(",");
                sb.append(ar.getCheckInTime() != null ? DATE_FORMAT.format(ar.getCheckInTime()) : "N/A").append(",");
                sb.append(escapeCSV(ar.getGymId())).append(",");
                sb.append(escapeCSV(ar.getMemberName())).append(",");
                sb.append(escapeCSV(ar.getMemberEmail())).append(",");
                sb.append("PRESENT");

                writer.write(sb.toString());
                writer.newLine();
            }
        } catch (IOException e) {
            throw new GymException("Failed to export attendance records to CSV: " + e.getMessage(), e);
        }
    }

    // ==========================================
    // Character Streams - Reading & Validation (Lectures 25-27)
    // ==========================================

    /**
     * Reads a CSV file using BufferedReader and FileReader, parses lines, validates records,
     * and constructs an in-memory List collection (Lectures 25, 26, 27).
     */
    public static List<AttendanceRecord> importAttendanceFromCSV(File csvFile) throws GymException {
        if (csvFile == null || !csvFile.exists()) {
            throw new ValidationException("Selected CSV file does not exist.");
        }

        List<AttendanceRecord> importedList = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(csvFile))) {
            String line = reader.readLine(); // Header line
            if (line == null) {
                throw new ValidationException("CSV file is empty.");
            }

            int lineNum = 1;
            while ((line = reader.readLine()) != null) {
                lineNum++;
                line = line.trim();
                if (line.isEmpty()) continue;

                // Simple comma split
                String[] tokens = line.split(",", -1);
                if (tokens.length < 5) {
                    throw new ValidationException("Invalid CSV format at line " + lineNum + ": minimum 5 columns expected.");
                }

                int id = 0;
                try {
                    id = Integer.parseInt(tokens[0].trim());
                } catch (NumberFormatException ignored) {}

                Date date;
                try {
                    date = DATE_FORMAT.parse(tokens[1].trim());
                } catch (ParseException e) {
                    date = new Date();
                }

                String gymId = tokens[2].trim().replace("\"", "");
                String name = tokens[3].trim().replace("\"", "");
                String email = tokens[4].trim().replace("\"", "");

                importedList.add(new AttendanceRecord(id, gymId, name, email, new Timestamp(date.getTime())));
            }
        } catch (IOException e) {
            throw new GymException("Error reading CSV file: " + e.getMessage(), e);
        }

        return importedList;
    }

    // ==========================================
    // Byte Streams - Binary File I/O (Lecture 25)
    // ==========================================

    /**
     * Saves raw binary audit log / receipt stamp using FileOutputStream (Lecture 25 - Byte streams).
     */
    public static void saveBinaryReceiptStamp(File targetFile, byte[] rawBytes) throws GymException {
        if (rawBytes == null) {
            throw new ValidationException("Raw receipt data is null.");
        }
        try (FileOutputStream fos = new FileOutputStream(targetFile)) {
            fos.write(rawBytes);
            fos.flush();
        } catch (IOException e) {
            throw new GymException("Failed to write binary receipt stream: " + e.getMessage(), e);
        }
    }

    /**
     * Reads raw binary data using FileInputStream (Lecture 25 - Byte streams).
     */
    public static byte[] readBinaryReceiptStamp(File sourceFile) throws GymException {
        if (!sourceFile.exists()) {
            throw new ValidationException("Receipt file not found.");
        }
        try (FileInputStream fis = new FileInputStream(sourceFile);
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[1024];
            int read;
            while ((read = fis.read(buffer)) != -1) {
                baos.write(buffer, 0, read);
            }
            return baos.toByteArray();
        } catch (IOException e) {
            throw new GymException("Failed to read binary receipt stream: " + e.getMessage(), e);
        }
    }

    private static String escapeCSV(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
