package cit.edu.workforce.Service;

import cit.edu.workforce.DTO.TrainingProgramDTO;
import cit.edu.workforce.Entity.DepartmentEntity;
import cit.edu.workforce.Entity.EmployeeEntity;
import cit.edu.workforce.Entity.TrainingEnrollmentEntity;
import cit.edu.workforce.Entity.TrainingProgramEntity;
import cit.edu.workforce.Repository.DepartmentRepository;
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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TrainingProgramService {

    private final TrainingProgramRepository trainingProgramRepository;
    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final TrainingEnrollmentRepository trainingEnrollmentRepository;

    @Autowired
    public TrainingProgramService(
            TrainingProgramRepository trainingProgramRepository,
            EmployeeRepository employeeRepository,
            DepartmentRepository departmentRepository,
            TrainingEnrollmentRepository trainingEnrollmentRepository) {
        this.trainingProgramRepository = trainingProgramRepository;
        this.employeeRepository = employeeRepository;
        this.departmentRepository = departmentRepository;
        this.trainingEnrollmentRepository = trainingEnrollmentRepository;
    }

    /**
     * Convert a TrainingProgramEntity to TrainingProgramDTO
     */
    public TrainingProgramDTO convertToDTO(TrainingProgramEntity program) {
        if (program == null) {
            return null;
        }

        TrainingProgramDTO dto = new TrainingProgramDTO();
        dto.setProgramId(program.getProgramId());
        dto.setProgramName(program.getProgramName());
        dto.setDescription(program.getDescription());
        dto.setProgramType(program.getProgramType());
        dto.setCategory(program.getCategory());
        dto.setDeliveryMethod(program.getDeliveryMethod());
        dto.setProvider(program.getProvider());
        dto.setDurationHours(program.getDurationHours());
        dto.setStartDate(program.getStartDate());
        dto.setEndDate(program.getEndDate());
        dto.setLocation(program.getLocation());
        dto.setInstructor(program.getInstructor());
        dto.setMaxParticipants(program.getMaxParticipants());
        dto.setPrerequisites(program.getPrerequisites());
        dto.setMaterialsUrl(program.getMaterialsUrl());
        dto.setCertificationOffered(program.getCertificationOffered());
        dto.setCertificationName(program.getCertificationName());
        dto.setCertificationValidityMonths(program.getCertificationValidityMonths());
        dto.setIsMandatory(program.getIsMandatory());
        dto.setIsActive(program.getIsActive());
        
        if (program.getCreatedBy() != null) {
            dto.setCreatedById(program.getCreatedBy().getEmployeeId());
            dto.setCreatedByName(program.getCreatedBy().getFirstName() + " " + program.getCreatedBy().getLastName());
        }
        
        // Set applicable departments
        if (program.getApplicableDepartments() != null && !program.getApplicableDepartments().isEmpty()) {
            dto.setApplicableDepartmentIds(
                    program.getApplicableDepartments().stream()
                            .map(DepartmentEntity::getDepartmentId)
                            .collect(Collectors.toList())
            );
            
            dto.setApplicableDepartmentNames(
                    program.getApplicableDepartments().stream()
                            .map(DepartmentEntity::getDepartmentName)
                            .collect(Collectors.toList())
            );
        } else {
            dto.setApplicableDepartmentIds(new ArrayList<>());
            dto.setApplicableDepartmentNames(new ArrayList<>());
        }
        
        // Set enrollments count
        dto.setEnrollmentsCount(program.getEnrollments() != null ? program.getEnrollments().size() : 0);
        
        return dto;
    }

    /**
     * Get all training programs (paginated)
     */
    public Page<TrainingProgramDTO> getAllTrainingPrograms(Pageable pageable) {
        Page<TrainingProgramEntity> programsPage = trainingProgramRepository.findAll(pageable);
        List<TrainingProgramDTO> dtoList = programsPage.getContent().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        
        return new PageImpl<>(dtoList, pageable, programsPage.getTotalElements());
    }

    /**
     * Get active training programs (paginated)
     */
    public Page<TrainingProgramDTO> getActiveTrainingPrograms(Pageable pageable) {
        Page<TrainingProgramEntity> programsPage = trainingProgramRepository.findByIsActive(true, pageable);
        List<TrainingProgramDTO> dtoList = programsPage.getContent().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        
        return new PageImpl<>(dtoList, pageable, programsPage.getTotalElements());
    }

    /**
     * Get training program by ID
     */
    public Optional<TrainingProgramDTO> getTrainingProgramById(String programId) {
        return trainingProgramRepository.findById(programId)
                .map(this::convertToDTO);
    }

    /**
     * Get training programs by type (paginated)
     */
    public Page<TrainingProgramDTO> getTrainingProgramsByType(String programType, Pageable pageable) {
        Page<TrainingProgramEntity> programsPage = trainingProgramRepository.findByProgramType(programType, pageable);
        List<TrainingProgramDTO> dtoList = programsPage.getContent().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        
        return new PageImpl<>(dtoList, pageable, programsPage.getTotalElements());
    }

    /**
     * Get training programs by delivery method (paginated)
     */
    public Page<TrainingProgramDTO> getTrainingProgramsByDeliveryMethod(String deliveryMethod, Pageable pageable) {
        Page<TrainingProgramEntity> programsPage = trainingProgramRepository.findByDeliveryMethod(deliveryMethod, pageable);
        List<TrainingProgramDTO> dtoList = programsPage.getContent().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        
        return new PageImpl<>(dtoList, pageable, programsPage.getTotalElements());
    }

    /**
     * Get mandatory training programs (paginated)
     */
    public Page<TrainingProgramDTO> getMandatoryTrainingPrograms(Pageable pageable) {
        Page<TrainingProgramEntity> programsPage = trainingProgramRepository.findByIsMandatory(true, pageable);
        List<TrainingProgramDTO> dtoList = programsPage.getContent().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        
        return new PageImpl<>(dtoList, pageable, programsPage.getTotalElements());
    }

    /**
     * Get training programs that offer certification (paginated)
     */
    public Page<TrainingProgramDTO> getTrainingProgramsWithCertification(Pageable pageable) {
        Page<TrainingProgramEntity> programsPage = trainingProgramRepository.findByCertificationOffered(true, pageable);
        List<TrainingProgramDTO> dtoList = programsPage.getContent().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        
        return new PageImpl<>(dtoList, pageable, programsPage.getTotalElements());
    }

    /**
     * Get upcoming training programs (paginated)
     */
    public Page<TrainingProgramDTO> getUpcomingTrainingPrograms(Pageable pageable) {
        LocalDate today = LocalDate.now();
        Page<TrainingProgramEntity> programsPage = trainingProgramRepository.findByStartDateBetween(
                today, today.plusMonths(3), pageable);
        List<TrainingProgramDTO> dtoList = programsPage.getContent().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        
        return new PageImpl<>(dtoList, pageable, programsPage.getTotalElements());
    }

    /**
     * Get training programs by department (paginated)
     */
    public Page<TrainingProgramDTO> getTrainingProgramsByDepartment(String departmentId, Pageable pageable) {
        DepartmentEntity department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Department not found"));
        
        Page<TrainingProgramEntity> programsPage = trainingProgramRepository.findByApplicableDepartment(department, pageable);
        List<TrainingProgramDTO> dtoList = programsPage.getContent().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        
        return new PageImpl<>(dtoList, pageable, programsPage.getTotalElements());
    }

    /**
     * Search training programs by name or description (paginated)
     */
    public Page<TrainingProgramDTO> searchTrainingPrograms(String searchTerm, Pageable pageable) {
        Page<TrainingProgramEntity> programsPage = trainingProgramRepository
                .findByProgramNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(searchTerm, searchTerm, pageable);
        List<TrainingProgramDTO> dtoList = programsPage.getContent().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        
        return new PageImpl<>(dtoList, pageable, programsPage.getTotalElements());
    }

    /**
     * Create a new training program
     */
    @Transactional
    public TrainingProgramDTO createTrainingProgram(
            String programName,
            String description,
            String programType,
            String category,
            String deliveryMethod,
            String provider,
            Integer durationHours,
            LocalDate startDate,
            LocalDate endDate,
            String location,
            String instructor,
            Integer maxParticipants,
            String prerequisites,
            String materialsUrl,
            Boolean certificationOffered,
            String certificationName,
            Integer certificationValidityMonths,
            Boolean isMandatory,
            Boolean isActive,
            String createdById,
            List<String> applicableDepartmentIds) {
        
        EmployeeEntity createdBy = employeeRepository.findById(createdById)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee not found"));
        
        TrainingProgramEntity program = new TrainingProgramEntity();
        program.setProgramName(programName);
        program.setDescription(description);
        program.setProgramType(programType);
        program.setCategory(category);
        program.setDeliveryMethod(deliveryMethod);
        program.setProvider(provider);
        program.setDurationHours(durationHours);
        program.setStartDate(startDate);
        program.setEndDate(endDate);
        program.setLocation(location);
        program.setInstructor(instructor);
        program.setMaxParticipants(maxParticipants);
        program.setPrerequisites(prerequisites);
        program.setMaterialsUrl(materialsUrl);
        program.setCertificationOffered(certificationOffered != null ? certificationOffered : false);
        program.setCertificationName(certificationName);
        program.setCertificationValidityMonths(certificationValidityMonths);
        program.setIsMandatory(isMandatory != null ? isMandatory : false);
        program.setIsActive(isActive != null ? isActive : true);
        program.setCreatedBy(createdBy);
        program.setCreatedAt(LocalDateTime.now());
        
        // Set applicable departments if provided
        if (applicableDepartmentIds != null && !applicableDepartmentIds.isEmpty()) {
            program.setApplicableDepartments(new HashSet<>());
            for (String deptId : applicableDepartmentIds) {
                departmentRepository.findById(deptId).ifPresent(
                        department -> program.getApplicableDepartments().add(department)
                );
            }
        }
        
        TrainingProgramEntity savedProgram = trainingProgramRepository.save(program);
        return convertToDTO(savedProgram);
    }

    /**
     * Update an existing training program
     */
    @Transactional
    public TrainingProgramDTO updateTrainingProgram(
            String programId,
            String programName,
            String description,
            String programType,
            String category,
            String deliveryMethod,
            String provider,
            Integer durationHours,
            LocalDate startDate,
            LocalDate endDate,
            String location,
            String instructor,
            Integer maxParticipants,
            String prerequisites,
            String materialsUrl,
            Boolean certificationOffered,
            String certificationName,
            Integer certificationValidityMonths,
            Boolean isMandatory,
            Boolean isActive,
            List<String> applicableDepartmentIds) {
        
        TrainingProgramEntity program = trainingProgramRepository.findById(programId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Training program not found"));
        
        if (programName != null) program.setProgramName(programName);
        if (description != null) program.setDescription(description);
        if (programType != null) program.setProgramType(programType);
        if (category != null) program.setCategory(category);
        if (deliveryMethod != null) program.setDeliveryMethod(deliveryMethod);
        if (provider != null) program.setProvider(provider);
        if (durationHours != null) program.setDurationHours(durationHours);
        if (startDate != null) program.setStartDate(startDate);
        if (endDate != null) program.setEndDate(endDate);
        if (location != null) program.setLocation(location);
        if (instructor != null) program.setInstructor(instructor);
        if (maxParticipants != null) program.setMaxParticipants(maxParticipants);
        if (prerequisites != null) program.setPrerequisites(prerequisites);
        if (materialsUrl != null) program.setMaterialsUrl(materialsUrl);
        if (certificationOffered != null) program.setCertificationOffered(certificationOffered);
        if (certificationName != null) program.setCertificationName(certificationName);
        if (certificationValidityMonths != null) program.setCertificationValidityMonths(certificationValidityMonths);
        if (isMandatory != null) program.setIsMandatory(isMandatory);
        if (isActive != null) program.setIsActive(isActive);
        
        // Update applicable departments if provided
        if (applicableDepartmentIds != null) {
            program.getApplicableDepartments().clear();
            for (String deptId : applicableDepartmentIds) {
                departmentRepository.findById(deptId).ifPresent(
                        department -> program.getApplicableDepartments().add(department)
                );
            }
        }
        
        program.setUpdatedAt(LocalDateTime.now());
        TrainingProgramEntity updatedProgram = trainingProgramRepository.save(program);
        return convertToDTO(updatedProgram);
    }

    /**
     * Delete a training program
     */
    @Transactional
    public void deleteTrainingProgram(String programId) {
        TrainingProgramEntity program = trainingProgramRepository.findById(programId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Training program not found"));
        
        // Check if there are enrollments
        List<TrainingEnrollmentEntity> enrollments = trainingEnrollmentRepository.findByTrainingProgram(program);
        if (enrollments != null && !enrollments.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                    "Cannot delete program with active enrollments. Deactivate the program instead.");
        }
        
        trainingProgramRepository.delete(program);
    }

    /**
     * Activate/deactivate a training program
     */
    @Transactional
    public TrainingProgramDTO setTrainingProgramActiveStatus(String programId, boolean isActive) {
        TrainingProgramEntity program = trainingProgramRepository.findById(programId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Training program not found"));
        
        program.setIsActive(isActive);
        program.setUpdatedAt(LocalDateTime.now());
        
        TrainingProgramEntity updatedProgram = trainingProgramRepository.save(program);
        return convertToDTO(updatedProgram);
    }

    /**
     * Get all training programs created by an employee
     */
    public List<TrainingProgramDTO> getTrainingProgramsByCreator(String employeeId) {
        EmployeeEntity employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee not found"));
        
        List<TrainingProgramEntity> programs = trainingProgramRepository.findByCreatedBy(employee);
        return programs.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
}

// New file: Service class for managing training programs in Module 7: Training & Development
// Provides business logic for creating, retrieving, updating, and managing training programs 