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
    public static String hash(String password) throws NoSuchAlgorithmException {
        byte[] salt = generateSalt();
        byte[] hashBytes = hashWithSalt(password, salt);
        return Base64.getEncoder().encodeToString(salt)
                + ":"
                + Base64.getEncoder().encodeToString(hashBytes);
    }

    public static boolean verify(String inputPassword, String storedHash) throws NoSuchAlgorithmException {
        String[] parts = storedHash.split(":");
        byte[] salt = Base64.getDecoder().decode(parts[0]);
        byte[] hashBytes = Base64.getDecoder().decode(parts[1]);
        byte[] inputHashBytes = hashWithSalt(inputPassword, salt);
        return MessageDigest.isEqual(hashBytes, inputHashBytes);
    }

    private static byte[] generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[16];
        random.nextBytes(salt);
        return salt;
    }

    private static byte[] hashWithSalt(String password, byte[] salt) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        digest.update(salt);
        return digest.digest(password.getBytes(StandardCharsets.UTF_8));
    }
}
