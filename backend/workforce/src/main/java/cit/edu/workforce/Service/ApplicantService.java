package cit.edu.workforce.Service;

import cit.edu.workforce.DTO.ApplicantDTO;
import cit.edu.workforce.Entity.ApplicantEntity;
import cit.edu.workforce.Entity.UserAccountEntity;
import cit.edu.workforce.Repository.ApplicantRepository;
import cit.edu.workforce.Repository.ApplicationRecordRepository;
import cit.edu.workforce.Repository.UserAccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * ApplicantService - Service for managing applicants
 * New file: This service provides methods to manage applicants including CRUD operations and search functionality
 */
@Service
public class ApplicantService {

    private final ApplicantRepository applicantRepository;
    private final UserAccountRepository userAccountRepository;
    private final ApplicationRecordRepository applicationRecordRepository;
    private final String uploadDirectory = "uploads/resumes";

    @Autowired
    public ApplicantService(
            ApplicantRepository applicantRepository,
            UserAccountRepository userAccountRepository,
            ApplicationRecordRepository applicationRecordRepository) {
        this.applicantRepository = applicantRepository;
        this.userAccountRepository = userAccountRepository;
        this.applicationRecordRepository = applicationRecordRepository;
        
        // Create the upload directory if it doesn't exist
        File directory = new File(uploadDirectory);
        if (!directory.exists()) {
            directory.mkdirs();
        }
    }

    /**
     * Get all applicants
     *
     * @return List of applicant DTOs
     */
    @Transactional(readOnly = true)
    public List<ApplicantDTO> getAllApplicants() {
        return applicantRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get paginated applicants
     *
     * @param pageable Pagination information
     * @return Page of applicant DTOs
     */
    @Transactional(readOnly = true)
    public Page<ApplicantDTO> getAllApplicants(Pageable pageable) {
        return applicantRepository.findAll(pageable)
                .map(this::convertToDTO);
    }

    /**
     * Get applicant by ID
     *
     * @param applicantId Applicant ID
     * @return Applicant DTO
     */
    @Transactional(readOnly = true)
    public ApplicantDTO getApplicantById(String applicantId) {
        ApplicantEntity applicant = applicantRepository.findById(applicantId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Applicant not found with ID: " + applicantId));

        return convertToDTO(applicant);
    }

    /**
     * Get internal applicants
     *
     * @return List of internal applicant DTOs
     */
    @Transactional(readOnly = true)
    public List<ApplicantDTO> getInternalApplicants() {
        return applicantRepository.findByIsInternal(true).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get paginated internal applicants
     *
     * @param pageable Pagination information
     * @return Page of internal applicant DTOs
     */
    @Transactional(readOnly = true)
    public Page<ApplicantDTO> getInternalApplicants(Pageable pageable) {
        return applicantRepository.findByIsInternal(true, pageable)
                .map(this::convertToDTO);
    }

    /**
     * Get external applicants
     *
     * @return List of external applicant DTOs
     */
    @Transactional(readOnly = true)
    public List<ApplicantDTO> getExternalApplicants() {
        return applicantRepository.findByIsInternal(false).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get paginated external applicants
     *
     * @param pageable Pagination information
     * @return Page of external applicant DTOs
     */
    @Transactional(readOnly = true)
    public Page<ApplicantDTO> getExternalApplicants(Pageable pageable) {
        return applicantRepository.findByIsInternal(false, pageable)
                .map(this::convertToDTO);
    }

    /**
     * Search applicants by name
     *
     * @param name     Name to search for
     * @param pageable Pagination information
     * @return Page of applicant DTOs
     */
    @Transactional(readOnly = true)
    public Page<ApplicantDTO> searchApplicantsByName(String name, Pageable pageable) {
        return applicantRepository.findByFullNameContainingIgnoreCase(name, pageable)
                .map(this::convertToDTO);
    }

    /**
     * Create a new internal applicant
     *
     * @param userId User ID (internal applicant)
     * @param phoneNumber Phone number
     * @param resumeFile Resume file
     * @return Created applicant DTO
     */
    @Transactional
    public ApplicantDTO createInternalApplicant(String userId, String phoneNumber, MultipartFile resumeFile)
            throws IOException {
        // Get user account
        UserAccountEntity user = getUserAccount(userId);
        
        // Check if the user is already an applicant
        if (applicantRepository.findByUser(user).isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "User is already registered as an applicant");
        }

        // Create new applicant
        ApplicantEntity applicant = new ApplicantEntity();
        applicant.setUser(user);
        applicant.setFullName(user.getEmailAddress().split("@")[0]); // Temporary, should be updated with real name
        applicant.setEmail(user.getEmailAddress());
        applicant.setPhoneNumber(phoneNumber);
        applicant.setInternal(true);
        applicant.setApplicationDate(LocalDate.now());

        // Handle resume upload if provided
        if (resumeFile != null && !resumeFile.isEmpty()) {
            String resumePath = saveResume(resumeFile);
            applicant.setResumePdfPath(resumePath);
        }

        // Save and return
        ApplicantEntity savedApplicant = applicantRepository.save(applicant);
        return convertToDTO(savedApplicant);
    }
    
    /**
     * Get user account by ID or use the current authenticated user if ID is null
     *
     * @param userId User ID (can be null)
     * @return User account entity
     */
    private UserAccountEntity getUserAccount(String userId) {
        if (userId != null) {
            return userAccountRepository.findById(userId)
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND, "User not found with ID: " + userId));
        }
        
        // Get current authenticated user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not authenticated");
        }
        
        String email = authentication.getName();
        return userAccountRepository.findByEmailAddress(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "User not found with email: " + email));
    }

    /**
     * Create a new external applicant
     *
     * @param fullName    Full name
     * @param email       Email address
     * @param phoneNumber Phone number
     * @param resumeFile  Resume file
     * @return Created applicant DTO
     */
    @Transactional
    public ApplicantDTO createExternalApplicant(String fullName, String email, String phoneNumber,
            MultipartFile resumeFile) throws IOException {
        // Validate input
        if (fullName == null || fullName.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Full name is required");
        }

        if (email == null || email.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email is required");
        }

        // Check if email is already registered
        if (applicantRepository.findByEmail(email).isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "An applicant with this email already exists");
        }

        // Create new applicant
        ApplicantEntity applicant = new ApplicantEntity();
        applicant.setFullName(fullName);
        applicant.setEmail(email);
        applicant.setPhoneNumber(phoneNumber);
        applicant.setInternal(false);
        applicant.setApplicationDate(LocalDate.now());

        // Handle resume upload if provided
        if (resumeFile != null && !resumeFile.isEmpty()) {
            String resumePath = saveResume(resumeFile);
            applicant.setResumePdfPath(resumePath);
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Resume is required for external applicants");
        }

        // Save and return
        ApplicantEntity savedApplicant = applicantRepository.save(applicant);
        return convertToDTO(savedApplicant);
    }

    /**
     * Update an applicant's information
     *
     * @param applicantId  Applicant ID
     * @param fullName     Full name
     * @param email        Email address
     * @param phoneNumber  Phone number
     * @param resumeFile   Resume file
     * @return Updated applicant DTO
     */
    @Transactional
    public ApplicantDTO updateApplicant(
            String applicantId, 
            String fullName, 
            String email, 
            String phoneNumber,
            MultipartFile resumeFile) throws IOException {
        
        // Get existing applicant
        ApplicantEntity applicant = applicantRepository.findById(applicantId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Applicant not found with ID: " + applicantId));

        // Check if the current user has permission to update this applicant
        if (!hasPermissionToModifyApplicant(applicant)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "You don't have permission to update this applicant");
        }

        // Update fields if provided
        if (fullName != null && !fullName.trim().isEmpty()) {
            applicant.setFullName(fullName);
        }

        if (email != null && !email.trim().isEmpty() && !email.equals(applicant.getEmail())) {
            // Check if email is already registered by another applicant
            applicantRepository.findByEmail(email).ifPresent(existingApplicant -> {
                if (!existingApplicant.getApplicantId().equals(applicantId)) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "An applicant with this email already exists");
                }
            });
            applicant.setEmail(email);
        }

        if (phoneNumber != null) {
            applicant.setPhoneNumber(phoneNumber);
        }

        // Handle resume upload if provided
        if (resumeFile != null && !resumeFile.isEmpty()) {
            String resumePath = saveResume(resumeFile);
            
            // Delete old resume file if exists
            if (applicant.getResumePdfPath() != null) {
                try {
                    Files.deleteIfExists(Paths.get(applicant.getResumePdfPath()));
                } catch (IOException e) {
                    // Log the error but continue
                    System.err.println("Failed to delete old resume file: " + e.getMessage());
                }
            }
            
            applicant.setResumePdfPath(resumePath);
        }

        // Save and return
        ApplicantEntity updatedApplicant = applicantRepository.save(applicant);
        return convertToDTO(updatedApplicant);
    }

    /**
     * Delete an applicant
     *
     * @param applicantId Applicant ID
     */
    @Transactional
    public void deleteApplicant(String applicantId) {
        // Get existing applicant
        ApplicantEntity applicant = applicantRepository.findById(applicantId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Applicant not found with ID: " + applicantId));

        // Check if the current user has permission to delete this applicant
        if (!hasPermissionToModifyApplicant(applicant)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "You don't have permission to delete this applicant");
        }

        // Check if there are any applications for this applicant
        long applicationCount = applicationRecordRepository.countByApplicant(applicant);
        if (applicationCount > 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Cannot delete applicant with existing applications");
        }

        // Delete resume file if exists
        if (applicant.getResumePdfPath() != null) {
            try {
                Files.deleteIfExists(Paths.get(applicant.getResumePdfPath()));
            } catch (IOException e) {
                // Log the error but continue
                System.err.println("Failed to delete resume file: " + e.getMessage());
            }
        }

        // Delete applicant
        applicantRepository.delete(applicant);
    }

    /**
     * Get current user's applicant profile
     *
     * @return Applicant DTO or null if not an applicant
     */
    @Transactional(readOnly = true)
    public ApplicantDTO getCurrentUserApplicantProfile() {
        // Get current authenticated user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not authenticated");
        }

        String email = authentication.getName();
        UserAccountEntity user = userAccountRepository.findByEmailAddress(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "User not found with email: " + email));

        // Find applicant profile for this user
        return applicantRepository.findByUser(user)
                .map(this::convertToDTO)
                .orElse(null);
    }

    /**
     * Check if the current user has permission to view the specified applicant
     *
     * @param applicantId Applicant ID
     * @return True if the current user has permission to view the applicant
     */
    @Transactional(readOnly = true)
    public boolean hasPermissionToViewApplicant(String applicantId) {
        ApplicantEntity applicant = applicantRepository.findById(applicantId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Applicant not found with ID: " + applicantId));
        
        return hasPermissionToViewApplicant(applicant);
    }
    
    /**
     * Check if the current user has permission to view the specified applicant
     *
     * @param applicant Applicant entity
     * @return True if the current user has permission to view the applicant
     */
    @Transactional(readOnly = true)
    public boolean hasPermissionToViewApplicant(ApplicantEntity applicant) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return false;
        }

        // HR and Admins have permission to view any applicant
        boolean isAdminOrHR = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_HR"));

        if (isAdminOrHR) {
            return true;
        }

        // Users can only view their own applicant profile
        String email = authentication.getName();
        return applicant.isInternal() && 
               applicant.getUser() != null && 
               applicant.getUser().getEmailAddress().equals(email);
    }
    
    /**
     * Check if the current user has permission to modify the specified applicant
     *
     * @param applicantId Applicant ID
     * @return True if the current user has permission to modify the applicant
     */
    @Transactional(readOnly = true)
    public boolean hasPermissionToModifyApplicant(String applicantId) {
        ApplicantEntity applicant = applicantRepository.findById(applicantId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Applicant not found with ID: " + applicantId));
        
        return hasPermissionToModifyApplicant(applicant);
    }

    /**
     * Save resume file
     *
     * @param resumeFile Resume file
     * @return Path to saved resume
     */
    private String saveResume(MultipartFile resumeFile) throws IOException {
        // Validate file type (e.g., PDF only)
        String contentType = resumeFile.getContentType();
        if (contentType == null || !contentType.equals("application/pdf")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only PDF files are allowed for resumes");
        }

        // Generate unique filename
        String filename = UUID.randomUUID().toString() + ".pdf";
        Path targetLocation = Paths.get(uploadDirectory).resolve(filename);

        // Save the file
        Files.copy(resumeFile.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

        return targetLocation.toString();
    }

    /**
     * Convert an applicant entity to DTO
     *
     * @param applicant Applicant entity
     * @return Applicant DTO
     */
    private ApplicantDTO convertToDTO(ApplicantEntity applicant) {
        ApplicantDTO dto = new ApplicantDTO();
        dto.setApplicantId(applicant.getApplicantId());
        dto.setFullName(applicant.getFullName());
        dto.setEmail(applicant.getEmail());
        dto.setPhoneNumber(applicant.getPhoneNumber());
        dto.setResumePdfPath(applicant.getResumePdfPath());
        dto.setInternal(applicant.isInternal());
        dto.setApplicationDate(applicant.getApplicationDate());
        
        if (applicant.getUser() != null) {
            dto.setUserId(applicant.getUser().getUserId());
        }
        
        // Get total number of applications for this applicant
        long totalApplications = applicationRecordRepository.countByApplicant(applicant);
        dto.setTotalApplications((int) totalApplications);
        
        return dto;
    }

    /**
     * Check if the current user has permission to modify the applicant
     *
     * @param applicant Applicant entity
     * @return True if the current user has permission, false otherwise
     */
    public boolean hasPermissionToModifyApplicant(ApplicantEntity applicant) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return false;
        }

        // HR and Admins have permission to modify any applicant
        boolean isAdminOrHR = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_HR"));

        if (isAdminOrHR) {
            return true;
        }

        // Users can only modify their own applicant profile
        String email = authentication.getName();
        return applicant.isInternal() && 
               applicant.getUser() != null && 
               applicant.getUser().getEmailAddress().equals(email);
    }
} 