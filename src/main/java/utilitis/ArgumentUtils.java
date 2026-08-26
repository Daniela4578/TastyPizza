package utilitis;

import java.math.BigDecimal;


public final class ArgumentUtils {

    private ArgumentUtils() {
        throw new AssertionError("Utility class");
    }

    public static String requireNonBlank(String value, String fieldName) {
        if (value == null || value.isBlank())
            throw new IllegalArgumentException(fieldName + " cannot be empty");
        return value.trim();
    }

    public static BigDecimal requirePositive(BigDecimal value, String fieldName) {
        if (value == null || value.compareTo(BigDecimal.ZERO) <= 0)
            throw new IllegalArgumentException(fieldName + " must be greater than zero");
        return value;
    }

    public static BigDecimal requireNonNegative(BigDecimal value, String fieldName) {
        if (value == null || value.compareTo(BigDecimal.ZERO) < 0)
            throw new IllegalArgumentException(fieldName + " cannot be negative");
        return value;
    }

    public static <T> T requireNonNull(T value, String fieldName) {
        if (value == null)
            throw new IllegalArgumentException(fieldName + " is required");
        return value;
    }
}