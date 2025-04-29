package cit.edu.workforce.Enum;

/**
 * EmploymentType - Enum for employment types
 * New file: This enum defines the types of employment available for job listings
 */
public enum EmploymentType {
    FULL_TIME("Full-time employment"),
    PART_TIME("Part-time employment"),
    CONTRACT("Contract employment");
    
    private final String description;
    
    EmploymentType(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
} 