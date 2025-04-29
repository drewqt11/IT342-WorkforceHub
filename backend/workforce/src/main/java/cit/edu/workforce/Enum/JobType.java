package cit.edu.workforce.Enum;

/**
 * JobType - Enum for job types
 * New file: This enum defines the types of jobs available (internal, external, or both)
 */
public enum JobType {
    INTERNAL("Visible only to internal employees"),
    EXTERNAL("Visible only to external candidates"),
    BOTH("Visible to both internal employees and external candidates");
    
    private final String description;
    
    JobType(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
} 