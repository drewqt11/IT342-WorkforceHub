package cit.edu.workforce.Service;

import cit.edu.workforce.DTO.TrainingEnrollmentDTO;
import cit.edu.workforce.Entity.EmployeeEntity;
import cit.edu.workforce.Entity.TrainingEnrollmentEntity;
import cit.edu.workforce.Entity.TrainingProgramEntity;
import cit.edu.workforce.Repository.EmployeeRepository;
import cit.edu.workforce.Repository.TrainingEnrollmentRepository;
import cit.edu.workforce.Repository.TrainingProgramRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TrainingEnrollmentService {

    private final TrainingEnrollmentRepository trainingEnrollmentRepository;
    private final TrainingProgramRepository trainingProgramRepository;
    private final EmployeeRepository employeeRepository;

    @Autowired
    public TrainingEnrollmentService(
            TrainingEnrollmentRepository trainingEnrollmentRepository,
            TrainingProgramRepository trainingProgramRepository,
            EmployeeRepository employeeRepository) {
        this.trainingEnrollmentRepository = trainingEnrollmentRepository;
        this.trainingProgramRepository = trainingProgramRepository;
        this.employeeRepository = employeeRepository;
    }

    /**
     * Convert a TrainingEnrollmentEntity to TrainingEnrollmentDTO
     */
    public TrainingEnrollmentDTO convertToDTO(TrainingEnrollmentEntity enrollment) {
        if (enrollment == null) {
            return null;
        }

        TrainingEnrollmentDTO dto = new TrainingEnrollmentDTO();
        dto.setEnrollmentId(enrollment.getEnrollmentId());
        
        if (enrollment.getEmployee() != null) {
            dto.setEmployeeId(enrollment.getEmployee().getEmployeeId());
            dto.setEmployeeName(enrollment.getEmployee().getFirstName() + " " + enrollment.getEmployee().getLastName());
        }
        
        if (enrollment.getTrainingProgram() != null) {
            dto.setProgramId(enrollment.getTrainingProgram().getProgramId());
            dto.setProgramName(enrollment.getTrainingProgram().getProgramName());
            dto.setProgramType(enrollment.getTrainingProgram().getProgramType());
        }
        
        dto.setEnrollmentDate(enrollment.getEnrollmentDate());
        dto.setCompletionDate(enrollment.getCompletionDate());
        dto.setDueDate(enrollment.getDueDate());
        dto.setStatus(enrollment.getStatus());
        dto.setCompletionPercentage(enrollment.getCompletionPercentage());
        dto.setScore(enrollment.getScore());
        dto.setCertificateUrl(enrollment.getCertificateUrl());
        dto.setCertificateExpiryDate(enrollment.getCertificateExpiryDate());
        dto.setFeedback(enrollment.getFeedback());
        dto.setInstructorComments(enrollment.getInstructorComments());
        
        if (enrollment.getAssignedBy() != null) {
            dto.setAssignedById(enrollment.getAssignedBy().getEmployeeId());
            dto.setAssignedByName(enrollment.getAssignedBy().getFirstName() + " " + enrollment.getAssignedBy().getLastName());
        }
        
        return dto;
    }

    /**
     * Get all training enrollments (paginated)
     */
    public Page<TrainingEnrollmentDTO> getAllTrainingEnrollments(Pageable pageable) {
        Page<TrainingEnrollmentEntity> enrollmentsPage = trainingEnrollmentRepository.findAll(pageable);
        List<TrainingEnrollmentDTO> dtoList = enrollmentsPage.getContent().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        
        return new PageImpl<>(dtoList, pageable, enrollmentsPage.getTotalElements());
    }

    /**
     * Get enrollment by ID
     */
    public Optional<TrainingEnrollmentDTO> getTrainingEnrollmentById(String enrollmentId) {
        return trainingEnrollmentRepository.findById(enrollmentId)
                .map(this::convertToDTO);
    }

    /**
     * Get enrollments by employee
     */
    public Page<TrainingEnrollmentDTO> getEnrollmentsByEmployee(String employeeId, Pageable pageable) {
        EmployeeEntity employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee not found"));
        
        Page<TrainingEnrollmentEntity> enrollmentsPage = trainingEnrollmentRepository.findByEmployee(employee, pageable);
        List<TrainingEnrollmentDTO> dtoList = enrollmentsPage.getContent().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        
        return new PageImpl<>(dtoList, pageable, enrollmentsPage.getTotalElements());
    }

    /**
     * Get enrollments by program
     */
    public Page<TrainingEnrollmentDTO> getEnrollmentsByProgram(String programId, Pageable pageable) {
        TrainingProgramEntity program = trainingProgramRepository.findById(programId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Training program not found"));
        
        Page<TrainingEnrollmentEntity> enrollmentsPage = trainingEnrollmentRepository.findByTrainingProgram(program, pageable);
        List<TrainingEnrollmentDTO> dtoList = enrollmentsPage.getContent().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        
        return new PageImpl<>(dtoList, pageable, enrollmentsPage.getTotalElements());
    }

    /**
     * Get enrollments by status
     */
    public Page<TrainingEnrollmentDTO> getEnrollmentsByStatus(String status, Pageable pageable) {
        Page<TrainingEnrollmentEntity> enrollmentsPage = trainingEnrollmentRepository.findByStatus(status, pageable);
        List<TrainingEnrollmentDTO> dtoList = enrollmentsPage.getContent().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        
        return new PageImpl<>(dtoList, pageable, enrollmentsPage.getTotalElements());
    }

    /**
     * Get enrollments by employee and status
     */
    public Page<TrainingEnrollmentDTO> getEnrollmentsByEmployeeAndStatus(String employeeId, String status, Pageable pageable) {
        EmployeeEntity employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee not found"));
        
        Page<TrainingEnrollmentEntity> enrollmentsPage = trainingEnrollmentRepository.findByEmployeeAndStatus(employee, status, pageable);
        List<TrainingEnrollmentDTO> dtoList = enrollmentsPage.getContent().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        
        return new PageImpl<>(dtoList, pageable, enrollmentsPage.getTotalElements());
    }

    /**
     * Get enrollments by program and status
     */
    public Page<TrainingEnrollmentDTO> getEnrollmentsByProgramAndStatus(String programId, String status, Pageable pageable) {
        TrainingProgramEntity program = trainingProgramRepository.findById(programId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Training program not found"));
        
        Page<TrainingEnrollmentEntity> enrollmentsPage = trainingEnrollmentRepository.findByTrainingProgramAndStatus(program, status, pageable);
        List<TrainingEnrollmentDTO> dtoList = enrollmentsPage.getContent().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        
        return new PageImpl<>(dtoList, pageable, enrollmentsPage.getTotalElements());
    }

    /**
     * Get enrollments approaching due date
     */
    public List<TrainingEnrollmentDTO> getEnrollmentsApproachingDueDate(LocalDate referenceDate) {
        if (referenceDate == null) {
            referenceDate = LocalDate.now().plusDays(7); // Default to 7 days from now
        }
        
        List<TrainingEnrollmentEntity> enrollments = trainingEnrollmentRepository.findByDueDateBefore(referenceDate);
        return enrollments.stream()
                .filter(e -> "ENROLLED".equals(e.getStatus()) || "IN_PROGRESS".equals(e.getStatus()))
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get completed enrollments in date range
     */
    public List<TrainingEnrollmentDTO> getCompletedEnrollmentsInDateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate == null) {
            startDate = LocalDate.now().minusMonths(1);
        }
        if (endDate == null) {
            endDate = LocalDate.now();
        }
        
        List<TrainingEnrollmentEntity> enrollments = trainingEnrollmentRepository.findByCompletionDateBetween(startDate, endDate);
        return enrollments.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get enrollments by assigned manager
     */
    public List<TrainingEnrollmentDTO> getEnrollmentsByAssignedManager(String managerId) {
        EmployeeEntity manager = employeeRepository.findById(managerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Manager not found"));
        
        List<TrainingEnrollmentEntity> enrollments = trainingEnrollmentRepository.findByAssignedBy(manager);
        return enrollments.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get expiring certifications
     */
    public List<TrainingEnrollmentDTO> getExpiringCertifications(String employeeId, LocalDate referenceDate) {
        if (referenceDate == null) {
            referenceDate = LocalDate.now().plusMonths(1); // Default to 1 month from now
        }
        
        EmployeeEntity employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee not found"));
        
        List<TrainingEnrollmentEntity> enrollments = trainingEnrollmentRepository.findByEmployeeAndCertificateExpiryDateBefore(
                employee, referenceDate);
        return enrollments.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Create a new training enrollment
     */
    @Transactional
    public TrainingEnrollmentDTO createTrainingEnrollment(String employeeId, String programId, String assignedById) {
        // Validate employee exists
        EmployeeEntity employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee not found"));
        
        // Validate program exists and is active
        TrainingProgramEntity program = trainingProgramRepository.findById(programId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Training program not found"));
        
        if (!program.getIsActive()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot enroll in inactive training program");
        }
        
        // Check if enrollment already exists
        if (trainingEnrollmentRepository.existsByEmployeeAndTrainingProgram(employee, program)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Employee is already enrolled in this program");
        }
        
        // Get the manager if provided
        EmployeeEntity assignedBy = null;
        if (assignedById != null) {
            assignedBy = employeeRepository.findById(assignedById)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Assigning manager not found"));
        }
        
        // Create enrollment
        TrainingEnrollmentEntity enrollment = new TrainingEnrollmentEntity();
        enrollment.setEmployee(employee);
        enrollment.setTrainingProgram(program);
        enrollment.setEnrollmentDate(LocalDate.now());
        enrollment.setStatus("ENROLLED");
        enrollment.setCompletionPercentage(0);
        
        // Set due date if program has end date
        if (program.getEndDate() != null) {
            enrollment.setDueDate(program.getEndDate());
        } else if (program.getStartDate() != null && program.getDurationHours() != null) {
            // Approximate due date based on duration if no end date
            int daysEstimate = Math.max(1, program.getDurationHours() / 8); // Assume 8 hours per day
            enrollment.setDueDate(program.getStartDate().plusDays(daysEstimate));
        }
        
        enrollment.setAssignedBy(assignedBy);
        
        TrainingEnrollmentEntity savedEnrollment = trainingEnrollmentRepository.save(enrollment);
        return convertToDTO(savedEnrollment);
    }

    /**
     * Update training enrollment progress
     */
    @Transactional
    public TrainingEnrollmentDTO updateEnrollmentProgress(
            String enrollmentId, 
            Integer completionPercentage, 
            String status,
            String feedback) {
        
        TrainingEnrollmentEntity enrollment = trainingEnrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Enrollment not found"));
        
        if (completionPercentage != null) {
            if (completionPercentage < 0 || completionPercentage > 100) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Completion percentage must be between 0 and 100");
            }
            enrollment.setCompletionPercentage(completionPercentage);
        }
        
        if (status != null) {
            enrollment.setStatus(status);
            
            // If status is COMPLETED, set completion date
            if ("COMPLETED".equals(status)) {
                enrollment.setCompletionDate(LocalDate.now());
                enrollment.setCompletionPercentage(100);
                
                // Set certificate expiry date if certification is offered
                if (enrollment.getTrainingProgram().getCertificationOffered() && 
                        enrollment.getTrainingProgram().getCertificationValidityMonths() != null) {
                    enrollment.setCertificateExpiryDate(
                            LocalDate.now().plusMonths(enrollment.getTrainingProgram().getCertificationValidityMonths())
                    );
                }
            }
        }
        
        if (feedback != null) {
            enrollment.setFeedback(feedback);
        }
        
        enrollment.setUpdatedAt(LocalDateTime.now());
        TrainingEnrollmentEntity updatedEnrollment = trainingEnrollmentRepository.save(enrollment);
        return convertToDTO(updatedEnrollment);
    }

    /**
     * Complete a training enrollment with assessment
     */
    @Transactional
    public TrainingEnrollmentDTO completeEnrollment(
            String enrollmentId, 
            Double score, 
            String instructorComments,
            String certificateUrl) {
        
        TrainingEnrollmentEntity enrollment = trainingEnrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Enrollment not found"));
        
        enrollment.setStatus("COMPLETED");
        enrollment.setCompletionDate(LocalDate.now());
        enrollment.setCompletionPercentage(100);
        
        if (score != null) {
            enrollment.setScore(score);
        }
        
        if (instructorComments != null) {
            enrollment.setInstructorComments(instructorComments);
        }
        
        if (certificateUrl != null) {
            enrollment.setCertificateUrl(certificateUrl);
            
            // Set certificate expiry date if certification is offered
            if (enrollment.getTrainingProgram().getCertificationOffered() && 
                    enrollment.getTrainingProgram().getCertificationValidityMonths() != null) {
                enrollment.setCertificateExpiryDate(
                        LocalDate.now().plusMonths(enrollment.getTrainingProgram().getCertificationValidityMonths())
                );
            }
        }
        
        enrollment.setUpdatedAt(LocalDateTime.now());
        TrainingEnrollmentEntity updatedEnrollment = trainingEnrollmentRepository.save(enrollment);
        return convertToDTO(updatedEnrollment);
    }

    /**
     * Update training enrollment details
     */
    @Transactional
    public TrainingEnrollmentDTO updateEnrollment(
            String enrollmentId, 
            LocalDate dueDate, 
            String status,
            Integer completionPercentage,
            Double score,
            String feedback,
            String instructorComments,
            String certificateUrl,
            LocalDate certificateExpiryDate) {
        
        TrainingEnrollmentEntity enrollment = trainingEnrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Enrollment not found"));
        
        if (dueDate != null) {
            enrollment.setDueDate(dueDate);
        }
        
        if (status != null) {
            enrollment.setStatus(status);
            
            // If status is COMPLETED, set completion date if not already set
            if ("COMPLETED".equals(status) && enrollment.getCompletionDate() == null) {
                enrollment.setCompletionDate(LocalDate.now());
                enrollment.setCompletionPercentage(100);
            }
        }
        
        if (completionPercentage != null) {
            if (completionPercentage < 0 || completionPercentage > 100) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Completion percentage must be between 0 and 100");
            }
            enrollment.setCompletionPercentage(completionPercentage);
        }
        
        if (score != null) {
            enrollment.setScore(score);
        }
        
        if (feedback != null) {
            enrollment.setFeedback(feedback);
        }
        
        if (instructorComments != null) {
            enrollment.setInstructorComments(instructorComments);
        }
        
        if (certificateUrl != null) {
            enrollment.setCertificateUrl(certificateUrl);
        }
        
        if (certificateExpiryDate != null) {
            enrollment.setCertificateExpiryDate(certificateExpiryDate);
        }
        
        enrollment.setUpdatedAt(LocalDateTime.now());
        TrainingEnrollmentEntity updatedEnrollment = trainingEnrollmentRepository.save(enrollment);
        return convertToDTO(updatedEnrollment);
    }

    /**
     * Delete a training enrollment
     */
    @Transactional
    public void deleteEnrollment(String enrollmentId) {
        TrainingEnrollmentEntity enrollment = trainingEnrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Enrollment not found"));
        
        trainingEnrollmentRepository.delete(enrollment);
    }

    /**
     * Cancel a training enrollment
     */
    @Transactional
    public TrainingEnrollmentDTO cancelEnrollment(String enrollmentId, String reason) {
        TrainingEnrollmentEntity enrollment = trainingEnrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Enrollment not found"));
        
        // Only enrollments with status ENROLLED or IN_PROGRESS can be cancelled
        if (!"ENROLLED".equals(enrollment.getStatus()) && !"IN_PROGRESS".equals(enrollment.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                    "Only enrollments with status ENROLLED or IN_PROGRESS can be cancelled");
        }
        
        enrollment.setStatus("WITHDRAWN");
        enrollment.setFeedback(reason != null ? "Cancelled: " + reason : "Cancelled by user");
        enrollment.setUpdatedAt(LocalDateTime.now());
        
        TrainingEnrollmentEntity updatedEnrollment = trainingEnrollmentRepository.save(enrollment);
        return convertToDTO(updatedEnrollment);
    }

    /**
     * Assign training to employee by manager
     */
    @Transactional
    public TrainingEnrollmentDTO assignTrainingToEmployee(String employeeId, String programId, String managerId, LocalDate dueDate) {
        // Validate employee exists
        EmployeeEntity employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee not found"));
        
        // Validate program exists and is active
        TrainingProgramEntity program = trainingProgramRepository.findById(programId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Training program not found"));
        
        if (!program.getIsActive()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot assign inactive training program");
        }
        
        // Validate manager exists
        EmployeeEntity manager = employeeRepository.findById(managerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Manager not found"));
        
        // Check if enrollment already exists
        Optional<TrainingEnrollmentEntity> existingEnrollment = 
                trainingEnrollmentRepository.findByEmployeeAndTrainingProgram(employee, program);
        
        if (existingEnrollment.isPresent()) {
            TrainingEnrollmentEntity enrollment = existingEnrollment.get();
            
            // If already completed, throw error
            if ("COMPLETED".equals(enrollment.getStatus()) || "FAILED".equals(enrollment.getStatus())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, 
                        "Employee has already completed or failed this training");
            }
            
            // If withdrawn, reactive it
            if ("WITHDRAWN".equals(enrollment.getStatus())) {
                enrollment.setStatus("ENROLLED");
                enrollment.setAssignedBy(manager);
                enrollment.setDueDate(dueDate != null ? dueDate : program.getEndDate());
                enrollment.setUpdatedAt(LocalDateTime.now());
                
                TrainingEnrollmentEntity updatedEnrollment = trainingEnrollmentRepository.save(enrollment);
                return convertToDTO(updatedEnrollment);
            }
            
            // Otherwise update the due date and assignedBy
            enrollment.setAssignedBy(manager);
            if (dueDate != null) {
                enrollment.setDueDate(dueDate);
            }
            enrollment.setUpdatedAt(LocalDateTime.now());
            
            TrainingEnrollmentEntity updatedEnrollment = trainingEnrollmentRepository.save(enrollment);
            return convertToDTO(updatedEnrollment);
        }
        
        // Create new enrollment
        TrainingEnrollmentEntity enrollment = new TrainingEnrollmentEntity();
        enrollment.setEmployee(employee);
        enrollment.setTrainingProgram(program);
        enrollment.setEnrollmentDate(LocalDate.now());
        enrollment.setStatus("ENROLLED");
        enrollment.setCompletionPercentage(0);
        enrollment.setAssignedBy(manager);
        
        // Set due date
        if (dueDate != null) {
            enrollment.setDueDate(dueDate);
        } else if (program.getEndDate() != null) {
            enrollment.setDueDate(program.getEndDate());
        } else {
            // Default due date: 30 days from now
            enrollment.setDueDate(LocalDate.now().plusDays(30));
        }
        
        TrainingEnrollmentEntity savedEnrollment = trainingEnrollmentRepository.save(enrollment);
        return convertToDTO(savedEnrollment);
    }
}

// New file: Service class for managing training enrollments in Module 7: Training & Development
// Provides business logic for enrolling employees in training programs, tracking progress, and managing certifications 