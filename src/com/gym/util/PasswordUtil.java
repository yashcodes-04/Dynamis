/**
 * Date: 2026-11-16 - Yash
 */
package com.gym.util;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

/**
 * Standard PBKDF2 password hasher with cryptographic salt.
 * Built into JDK with zero third-party library dependencies.
 */
public class PasswordUtil {

    private static final int ITERATIONS = 65536;
    private static final int KEY_LENGTH = 256;
    private static final String ALGORITHM = "PBKDF2WithHmacSHA256";

    public static String hashPassword(String password) {
        try {
            byte[] salt = new byte[16];
            SecureRandom random = new SecureRandom();
            random.nextBytes(salt);

            PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, ITERATIONS, KEY_LENGTH);
            SecretKeyFactory factory = SecretKeyFactory.getInstance(ALGORITHM);
            byte[] hash = factory.generateSecret(spec).getEncoded();

            String saltBase64 = Base64.getEncoder().encodeToString(salt);
            String hashBase64 = Base64.getEncoder().encodeToString(hash);

            return saltBase64 + ":" + hashBase64;
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException("Error hashing password: " + e.getMessage(), e);
        }
    }

    public static boolean verifyPassword(String plainPassword, String storedHash) {
        if (storedHash == null || !storedHash.contains(":")) {
            return false;
        }

        try {
            String[] parts = storedHash.split(":");
            byte[] salt = Base64.getDecoder().decode(parts[0]);
            byte[] expectedHash = Base64.getDecoder().decode(parts[1]);

            PBEKeySpec spec = new PBEKeySpec(plainPassword.toCharArray(), salt, ITERATIONS, KEY_LENGTH);
            SecretKeyFactory factory = SecretKeyFactory.getInstance(ALGORITHM);
            byte[] testHash = factory.generateSecret(spec).getEncoded();

            if (testHash.length != expectedHash.length) {
                return false;
            }

            int diff = 0;
            for (int i = 0; i < testHash.length; i++) {
                diff |= testHash[i] ^ expectedHash[i];
            }
            return diff == 0;
        } catch (Exception e) {
            return false;
        }
    }
}
