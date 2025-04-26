package cit.edu.workforce.Service;

import cit.edu.workforce.DTO.CertificateDTO;
import cit.edu.workforce.DTO.TrainingEnrollmentDTO;
import cit.edu.workforce.Entity.CertificateEntity;
import cit.edu.workforce.Entity.EmployeeEntity;
import cit.edu.workforce.Entity.EventEntity;
import cit.edu.workforce.Entity.TrainingEnrollmentEntity;
import cit.edu.workforce.Entity.TrainingProgramEntity;
import cit.edu.workforce.Repository.CertificateRepository;
import cit.edu.workforce.Repository.EmployeeRepository;
import cit.edu.workforce.Repository.EventRepository;
import cit.edu.workforce.Repository.TrainingEnrollmentRepository;
import cit.edu.workforce.Repository.TrainingProgramRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * TrainingEnrollmentService - Service for managing training enrollments
 */
@Service
@RequiredArgsConstructor
public class TrainingEnrollmentService {
    
    private final TrainingEnrollmentRepository trainingEnrollmentRepository;
    private final EmployeeRepository employeeRepository;
    private final TrainingProgramRepository trainingProgramRepository;
    private final EventRepository eventRepository;
    private final CertificateRepository certificateRepository;
    
    /**
     * Create a new training enrollment for a training program
     * 
     * @param trainingEnrollmentDTO The training enrollment data
     * @return The created enrollment DTO
     */
    @Transactional
    public TrainingEnrollmentDTO createTrainingProgramEnrollment(TrainingEnrollmentDTO trainingEnrollmentDTO) {
        // Validate employee
        EmployeeEntity employee = employeeRepository.findById(trainingEnrollmentDTO.getEmployeeId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee not found"));
        
        // Validate training program
        TrainingProgramEntity trainingProgram = trainingProgramRepository.findById(trainingEnrollmentDTO.getTrainingId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Training program not found"));
        
        // Check if already enrolled
        if (trainingEnrollmentRepository.findByEmployeeAndTrainingProgram(employee, trainingProgram).isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Employee is already enrolled in this training program");
        }
        
        // Create and save enrollment entity
        TrainingEnrollmentEntity enrollment = new TrainingEnrollmentEntity();
        enrollment.setEmployee(employee);
        enrollment.setTrainingProgram(trainingProgram);
        enrollment.setEnrollmentType(trainingEnrollmentDTO.getEnrollmentType());
        enrollment.setStatus("Enrolled");
        
        TrainingEnrollmentEntity savedEnrollment = trainingEnrollmentRepository.save(enrollment);
        
        return convertToDTO(savedEnrollment);
    }
    
    /**
     * Create a new training enrollment for an event
     * 
     * @param trainingEnrollmentDTO The training enrollment data
     * @return The created enrollment DTO
     */
    @Transactional
    public TrainingEnrollmentDTO createEventEnrollment(TrainingEnrollmentDTO trainingEnrollmentDTO) {
        // Validate employee
        EmployeeEntity employee = employeeRepository.findById(trainingEnrollmentDTO.getEmployeeId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee not found"));
        
        // Validate event
        EventEntity event = eventRepository.findById(trainingEnrollmentDTO.getEventId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found"));
        
        // Check if already enrolled
        if (trainingEnrollmentRepository.findByEmployeeAndEvent(employee, event).isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Employee is already enrolled in this event");
        }
        
        // Create and save enrollment entity
        TrainingEnrollmentEntity enrollment = new TrainingEnrollmentEntity();
        enrollment.setEmployee(employee);
        enrollment.setEvent(event);
        enrollment.setEnrollmentType(trainingEnrollmentDTO.getEnrollmentType());
        enrollment.setStatus("Enrolled");
        
        TrainingEnrollmentEntity savedEnrollment = trainingEnrollmentRepository.save(enrollment);
        
        return convertToDTO(savedEnrollment);
    }
    
    /**
     * Get enrollment by ID
     * 
     * @param enrollmentId The ID of the enrollment
     * @return Optional containing the enrollment if found
     */
    public Optional<TrainingEnrollmentDTO> getEnrollmentById(String enrollmentId) {
        return trainingEnrollmentRepository.findById(enrollmentId)
                .map(this::convertToDTO);
    }
    
    /**
     * Get all enrollments for an employee
     * 
     * @param employeeId The employee ID
     * @return List of enrollment DTOs
     */
    public List<TrainingEnrollmentDTO> getEnrollmentsByEmployee(String employeeId) {
        EmployeeEntity employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee not found"));
        
        return trainingEnrollmentRepository.findByEmployee(employee).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Get paginated enrollments for an employee
     * 
     * @param employeeId The employee ID
     * @param pageable Pagination information
     * @return Page of enrollment DTOs
     */
    public Page<TrainingEnrollmentDTO> getEnrollmentsByEmployee(String employeeId, Pageable pageable) {
        EmployeeEntity employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee not found"));
        
        return trainingEnrollmentRepository.findByEmployee(employee, pageable)
                .map(this::convertToDTO);
    }
    
    /**
     * Get all enrollments for a training program
     * 
     * @param trainingId The training program ID
     * @return List of enrollment DTOs
     */
    public List<TrainingEnrollmentDTO> getEnrollmentsByTrainingProgram(String trainingId) {
        TrainingProgramEntity trainingProgram = trainingProgramRepository.findById(trainingId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Training program not found"));
        
        return trainingEnrollmentRepository.findByTrainingProgram(trainingProgram).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Get paginated enrollments for a training program
     * 
     * @param trainingId The training program ID
     * @param pageable Pagination information
     * @return Page of enrollment DTOs
     */
    public Page<TrainingEnrollmentDTO> getEnrollmentsByTrainingProgram(String trainingId, Pageable pageable) {
        TrainingProgramEntity trainingProgram = trainingProgramRepository.findById(trainingId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Training program not found"));
        
        return trainingEnrollmentRepository.findByTrainingProgram(trainingProgram, pageable)
                .map(this::convertToDTO);
    }
    
    /**
     * Get all enrollments for an event
     * 
     * @param eventId The event ID
     * @return List of enrollment DTOs
     */
    public List<TrainingEnrollmentDTO> getEnrollmentsByEvent(String eventId) {
        EventEntity event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found"));
        
        return trainingEnrollmentRepository.findByEvent(event).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Get paginated enrollments for an event
     * 
     * @param eventId The event ID
     * @param pageable Pagination information
     * @return Page of enrollment DTOs
     */
    public Page<TrainingEnrollmentDTO> getEnrollmentsByEvent(String eventId, Pageable pageable) {
        EventEntity event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found"));
        
        return trainingEnrollmentRepository.findByEvent(event, pageable)
                .map(this::convertToDTO);
    }
    
    /**
     * Update enrollment status
     * 
     * @param enrollmentId The enrollment ID
     * @param status The new status
     * @return The updated enrollment DTO
     */
    @Transactional
    public TrainingEnrollmentDTO updateEnrollmentStatus(String enrollmentId, String status) {
        TrainingEnrollmentEntity enrollment = trainingEnrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Enrollment not found"));
        
        // Validate status
        if (!isValidStatus(status)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                    "Invalid status. Must be one of: Enrolled, Completed, Cancelled");
        }
        
        enrollment.setStatus(status);
        
        // If status is "Completed", set completion date
        if ("Completed".equals(status)) {
            enrollment.setCompletionDate(LocalDateTime.now());
        }
        
        TrainingEnrollmentEntity updatedEnrollment = trainingEnrollmentRepository.save(enrollment);
        return convertToDTO(updatedEnrollment);
    }
    
    /**
     * Cancel enrollment
     * 
     * @param enrollmentId The enrollment ID
     * @return The updated enrollment DTO
     */
    @Transactional
    public TrainingEnrollmentDTO cancelEnrollment(String enrollmentId) {
        return updateEnrollmentStatus(enrollmentId, "Cancelled");
    }
    
    /**
     * Complete enrollment
     * 
     * @param enrollmentId The enrollment ID
     * @return The updated enrollment DTO
     */
    @Transactional
    public TrainingEnrollmentDTO completeEnrollment(String enrollmentId) {
        return updateEnrollmentStatus(enrollmentId, "Completed");
    }
    
    /**
     * Add certificate to enrollment
     * 
     * @param enrollmentId The enrollment ID
     * @param certificateDTO The certificate data
     * @return The updated enrollment DTO
     */
    @Transactional
    public TrainingEnrollmentDTO addCertificate(String enrollmentId, CertificateDTO certificateDTO) {
        TrainingEnrollmentEntity enrollment = trainingEnrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Enrollment not found"));
        
        CertificateEntity certificate = new CertificateEntity();
        certificate.setFilePath(certificateDTO.getFilePath());
        certificate.setStatus("Pending");
        certificate.setRemarks(certificateDTO.getRemarks());
        certificate.setTrainingEnrollment(enrollment);
        
        certificateRepository.save(certificate);
        
        // Update enrollment status to completed
        enrollment.setStatus("Completed");
        enrollment.setCompletionDate(LocalDateTime.now());
        
        TrainingEnrollmentEntity updatedEnrollment = trainingEnrollmentRepository.save(enrollment);
        return convertToDTO(updatedEnrollment);
    }
    
    /**
     * Check if a status value is valid
     * 
     * @param status The status to check
     * @return true if valid, false otherwise
     */
    private boolean isValidStatus(String status) {
        return status != null && (status.equals("Enrolled") || status.equals("Completed") || status.equals("Cancelled"));
    }
    
    /**
     * Convert an entity to a DTO
     * 
     * @param entity The entity to convert
     * @return The converted DTO
     */
    private TrainingEnrollmentDTO convertToDTO(TrainingEnrollmentEntity entity) {
        TrainingEnrollmentDTO dto = new TrainingEnrollmentDTO();
        dto.setEnrollmentId(entity.getEnrollmentId());
        dto.setEnrolledDate(entity.getEnrolledDate());
        dto.setEnrollmentType(entity.getEnrollmentType());
        dto.setStatus(entity.getStatus());
        dto.setCompletionDate(entity.getCompletionDate());
        
        // Set employee information
        dto.setEmployeeId(entity.getEmployee().getEmployeeId());
        dto.setEmployeeName(entity.getEmployee().getFirstName() + " " + entity.getEmployee().getLastName());
        
        // Set training program information if available
        if (entity.getTrainingProgram() != null) {
            dto.setTrainingId(entity.getTrainingProgram().getTrainingId());
            dto.setTrainingTitle(entity.getTrainingProgram().getTitle());
            dto.setTrainingProvider(entity.getTrainingProgram().getProvider());
            dto.setTrainingMode(entity.getTrainingProgram().getTrainingMode());
        }
        
        // Set event information if available
        if (entity.getEvent() != null) {
            dto.setEventId(entity.getEvent().getEventId());
            dto.setEventType(entity.getEvent().getEventType());
            dto.setEventTitle(entity.getEvent().getTitle());
            dto.setEventDatetime(entity.getEvent().getEventDatetime());
        }
        
        // Get certificates
        List<CertificateDTO> certificateDTOs = entity.getCertificates().stream()
                .map(this::convertCertificateToDTO)
                .collect(Collectors.toList());
        dto.setCertificates(certificateDTOs);
        dto.setCertificateCount(certificateDTOs.size());
        
        return dto;
    }
    
    /**
     * Convert a certificate entity to a DTO
     * 
     * @param entity The entity to convert
     * @return The converted DTO
     */
    private CertificateDTO convertCertificateToDTO(CertificateEntity entity) {
        CertificateDTO dto = new CertificateDTO();
        dto.setCertificateId(entity.getCertificateId());
        dto.setFilePath(entity.getFilePath());
        dto.setUploadedAt(entity.getUploadedAt());
        dto.setStatus(entity.getStatus());
        dto.setVerifiedAt(entity.getVerifiedAt());
        dto.setRemarks(entity.getRemarks());
        dto.setEnrollmentId(entity.getTrainingEnrollment().getEnrollmentId());
        
        if (entity.getVerifiedBy() != null) {
            dto.setVerifiedById(entity.getVerifiedBy().getUserId());
            dto.setVerifiedByName(entity.getVerifiedBy().getEmailAddress());
        }
        
        return dto;
    }
} 