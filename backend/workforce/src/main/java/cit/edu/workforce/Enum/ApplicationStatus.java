package cit.edu.workforce.Enum;

/**
 * ApplicationStatus - Enum for application statuses
 * New file: This enum defines the statuses for job applications
 */
public enum ApplicationStatus {
    PENDING("Application is under review"),
    SHORTLISTED("Candidate has been shortlisted for interview"),
    REJECTED("Application has been rejected"),
    HIRED("Candidate has been hired");
    
    private final String description;
    
    ApplicationStatus(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
} 