package utilitis;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

public final class PasswordHasher {

    private PasswordHasher() {
        throw new AssertionError("Utility class");
    }

    // no longer throws checked exception — callers don't need try-catch
    public static String hash(String password) {
        try {
            byte[] salt      = generateSalt();
            byte[] hashBytes = hashWithSalt(password, salt);
            return Base64.getEncoder().encodeToString(salt)
                    + ":"
                    + Base64.getEncoder().encodeToString(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            // SHA-256 is guaranteed available on every Java SE platform
            // if this ever throws it is a JVM installation problem, not our bug
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }

    // no longer throws checked exception — callers don't need try-catch
    public static boolean verify(String inputPassword, String storedHash) {
        try {
            String[] parts        = storedHash.split(":");
            byte[]   salt         = Base64.getDecoder().decode(parts[0]);
            byte[]   savedHash    = Base64.getDecoder().decode(parts[1]);
            byte[]   inputHash    = hashWithSalt(inputPassword, salt);
            // timing-safe comparison — prevents timing attacks
            return MessageDigest.isEqual(savedHash, inputHash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }

    private static byte[] generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[]       salt   = new byte[16];
        random.nextBytes(salt);
        return salt;
    }

    private static byte[] hashWithSalt(String password, byte[] salt)
            throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        digest.update(salt);
        return digest.digest(password.getBytes(StandardCharsets.UTF_8));
    }
}