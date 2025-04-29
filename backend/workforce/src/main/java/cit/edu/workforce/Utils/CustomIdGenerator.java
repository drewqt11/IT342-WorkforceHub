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
     * Generates a benefit plan ID in the format "PLAN-XXXX-XXXXX" where X is a random hexadecimal character (0-9, a-f).
     * 
     * @return A formatted benefit plan ID string
     */
    public static String generateBenefitPlanId() {
        return "PLAN-" + generateRandomString(4) + "-" + generateRandomString(5);
    }
    
    /**
     * Generates a benefit enrollment ID in the format "BENR-XXXX-XXXXX" where X is a random hexadecimal character (0-9, a-f).
     * 
     * @return A formatted benefit enrollment ID string
     */
    public static String generateBenefitEnrollmentId() {
        return "BENR-" + generateRandomString(4) + "-" + generateRandomString(5);
    }
    
    /**
     * Generates a benefit dependent ID in the format "DPND-XXXX-XXXXX" where X is a random hexadecimal character (0-9, a-f).
     * 
     * @return A formatted benefit dependent ID string
     */
    public static String generateBenefitDependentId() {
        return "DPND-" + generateRandomString(4) + "-" + generateRandomString(5);
    }
    
    /**
     * Generates a reimbursement request ID in the format "REIM-XXXX-XXXXX" where X is a random hexadecimal character (0-9, a-f).
     * 
     * @return A formatted reimbursement request ID string
     */
    public static String generateReimbursementRequestId() {
        return "REIM-" + generateRandomString(4) + "-" + generateRandomString(5);
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