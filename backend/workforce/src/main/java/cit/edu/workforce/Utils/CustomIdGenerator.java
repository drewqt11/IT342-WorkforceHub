package cit.edu.workforce.Utils;

import java.security.SecureRandom;

/**
 * Utility class for generating custom IDs in specific formats.
 */
public class CustomIdGenerator {
    
    private static final String CHARS = "0123456789abcdef";
    private static final SecureRandom RANDOM = new SecureRandom();
    
    /**
     * Generates a user ID in the format "USER-XXXX-XXXXX" where X is a random hexadecimal character (0-9, a-f).
     * 
     * @return A formatted user ID string
     */
    public static String generateUserId() {
        return "USER-" + generateRandomString(4) + "-" + generateRandomString(5);
    }
    
    /**
     * Generates an employee ID in the format "EMPX-XXXX-XXXXX" where X is a random hexadecimal character (0-9, a-f).
     * 
     * @return A formatted employee ID string
     */
    public static String generateEmployeeId() {
        return "EMPX-" + generateRandomString(4) + "-" + generateRandomString(5);
    }
    
    /**
     * Generates a random string of specified length using hexadecimal characters.
     * 
     * @param length The length of the random string to generate
     * @return A random string
     */
    private static String generateRandomString(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(CHARS.charAt(RANDOM.nextInt(CHARS.length())));
        }
        return sb.toString();
    }
}
