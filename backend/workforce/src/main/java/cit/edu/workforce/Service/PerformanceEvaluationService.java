package cit.edu.workforce.Service;

import cit.edu.workforce.DTO.PerformanceEvaluationDTO;
import cit.edu.workforce.Entity.EmployeeEntity;
import cit.edu.workforce.Entity.PerformanceEvaluationEntity;
import cit.edu.workforce.Repository.EmployeeRepository;
import cit.edu.workforce.Repository.PerformanceEvaluationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PerformanceEvaluationService {

    private final PerformanceEvaluationRepository performanceEvaluationRepository;
    private final EmployeeRepository employeeRepository;

    @Autowired
    public PerformanceEvaluationService(
            PerformanceEvaluationRepository performanceEvaluationRepository,
            EmployeeRepository employeeRepository) {
        this.performanceEvaluationRepository = performanceEvaluationRepository;
        this.employeeRepository = employeeRepository;
    }

    // Convert entity to DTO
    public PerformanceEvaluationDTO convertToDTO(PerformanceEvaluationEntity evaluation) {
        PerformanceEvaluationDTO dto = new PerformanceEvaluationDTO();
        dto.setEvaluationId(evaluation.getEvaluationId());
        
        if (evaluation.getEmployee() != null) {
            dto.setEmployeeId(evaluation.getEmployee().getEmployeeId());
            dto.setEmployeeName(evaluation.getEmployee().getFirstName() + " " + evaluation.getEmployee().getLastName());
        }
        
        dto.setEvaluationType(evaluation.getEvaluationType());
        dto.setEvaluationPeriodStart(evaluation.getEvaluationPeriodStart());
        dto.setEvaluationPeriodEnd(evaluation.getEvaluationPeriodEnd());
        dto.setSubmittedDate(evaluation.getSubmittedDate());
        dto.setDueDate(evaluation.getDueDate());
        dto.setStatus(evaluation.getStatus());
        dto.setOverallRating(evaluation.getOverallRating());
        dto.setPerformanceSummary(evaluation.getPerformanceSummary());
        dto.setStrengths(evaluation.getStrengths());
        dto.setAreasForImprovement(evaluation.getAreasForImprovement());
        dto.setGoalsAchieved(evaluation.getGoalsAchieved());
        dto.setGoalsForNextPeriod(evaluation.getGoalsForNextPeriod());
        
        if (evaluation.getEvaluator() != null) {
            dto.setEvaluatorId(evaluation.getEvaluator().getEmployeeId());
            dto.setEvaluatorName(evaluation.getEvaluator().getFirstName() + " " + evaluation.getEvaluator().getLastName());
        }
        
        dto.setEvaluatorComments(evaluation.getEvaluatorComments());
        dto.setEmployeeComments(evaluation.getEmployeeComments());
        dto.setAcknowledgementDate(evaluation.getAcknowledgementDate());
        
        return dto;
    }

    @Transactional(readOnly = true)
    public List<PerformanceEvaluationDTO> getAllPerformanceEvaluations() {
        return performanceEvaluationRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<PerformanceEvaluationDTO> getAllPerformanceEvaluations(Pageable pageable) {
        return performanceEvaluationRepository.findAll(pageable)
                .map(this::convertToDTO);
    }

    @Transactional(readOnly = true)
    public Optional<PerformanceEvaluationDTO> getPerformanceEvaluationById(String evaluationId) {
        return performanceEvaluationRepository.findById(evaluationId)
                .map(this::convertToDTO);
    }

    @Transactional(readOnly = true)
    public List<PerformanceEvaluationDTO> getPerformanceEvaluationsByEmployee(String employeeId) {
        Optional<EmployeeEntity> employee = employeeRepository.findById(employeeId);
        if (employee.isPresent()) {
            return performanceEvaluationRepository.findByEmployee(employee.get()).stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
        }
        return List.of();
    }

    @Transactional(readOnly = true)
    public Page<PerformanceEvaluationDTO> getPerformanceEvaluationsByEmployee(String employeeId, Pageable pageable) {
        Optional<EmployeeEntity> employee = employeeRepository.findById(employeeId);
        if (employee.isPresent()) {
            return performanceEvaluationRepository.findByEmployee(employee.get(), pageable)
                    .map(this::convertToDTO);
        }
        return Page.empty();
    }

    @Transactional(readOnly = true)
    public List<PerformanceEvaluationDTO> getPerformanceEvaluationsByEvaluator(String evaluatorId) {
        Optional<EmployeeEntity> evaluator = employeeRepository.findById(evaluatorId);
        if (evaluator.isPresent()) {
            return performanceEvaluationRepository.findByEvaluator(evaluator.get()).stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
        }
        return List.of();
    }

    @Transactional(readOnly = true)
    public Page<PerformanceEvaluationDTO> getPerformanceEvaluationsByEvaluator(String evaluatorId, Pageable pageable) {
        Optional<EmployeeEntity> evaluator = employeeRepository.findById(evaluatorId);
        if (evaluator.isPresent()) {
            return performanceEvaluationRepository.findByEvaluator(evaluator.get(), pageable)
                    .map(this::convertToDTO);
        }
        return Page.empty();
    }

    @Transactional(readOnly = true)
    public List<PerformanceEvaluationDTO> getPerformanceEvaluationsByStatus(String status) {
        return performanceEvaluationRepository.findByStatus(status).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<PerformanceEvaluationDTO> getPerformanceEvaluationsByStatus(String status, Pageable pageable) {
        return performanceEvaluationRepository.findByStatus(status, pageable)
                .map(this::convertToDTO);
    }

    @Transactional(readOnly = true)
    public List<PerformanceEvaluationDTO> getPerformanceEvaluationsByType(String evaluationType) {
        return performanceEvaluationRepository.findByEvaluationType(evaluationType).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<PerformanceEvaluationDTO> getPerformanceEvaluationsByType(String evaluationType, Pageable pageable) {
        return performanceEvaluationRepository.findByEvaluationType(evaluationType, pageable)
                .map(this::convertToDTO);
    }

    @Transactional(readOnly = true)
    public List<PerformanceEvaluationDTO> getPendingEvaluations() {
        return performanceEvaluationRepository.findByDueDateBefore(LocalDate.now()).stream()
                .filter(evaluation -> !evaluation.getStatus().equals("COMPLETED") && 
                                     !evaluation.getStatus().equals("ACKNOWLEDGED"))
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public PerformanceEvaluationDTO createPerformanceEvaluation(
            String employeeId,
            String evaluationType,
            LocalDate evaluationPeriodStart,
            LocalDate evaluationPeriodEnd,
            LocalDate dueDate,
            String evaluatorId) {

        // Check if employee exists
        EmployeeEntity employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        PerformanceEvaluationEntity evaluation = new PerformanceEvaluationEntity();
        evaluation.setEmployee(employee);
        evaluation.setEvaluationType(evaluationType);
        evaluation.setEvaluationPeriodStart(evaluationPeriodStart);
        evaluation.setEvaluationPeriodEnd(evaluationPeriodEnd);
        evaluation.setDueDate(dueDate);
        evaluation.setStatus("DRAFT");
        
        // Set evaluator if provided
        if (evaluatorId != null) {
            employeeRepository.findById(evaluatorId).ifPresent(evaluation::setEvaluator);
        }

        PerformanceEvaluationEntity savedEvaluation = performanceEvaluationRepository.save(evaluation);
        return convertToDTO(savedEvaluation);
    }

    @Transactional
    public PerformanceEvaluationDTO updatePerformanceEvaluation(
            String evaluationId,
            String evaluationType,
            LocalDate evaluationPeriodStart,
            LocalDate evaluationPeriodEnd,
            LocalDate dueDate,
            String status,
            Integer overallRating,
            String performanceSummary,
            String strengths,
            String areasForImprovement,
            String goalsAchieved,
            String goalsForNextPeriod,
            String evaluatorId,
            String evaluatorComments) {

        PerformanceEvaluationEntity evaluation = performanceEvaluationRepository.findById(evaluationId)
                .orElseThrow(() -> new RuntimeException("Performance evaluation not found"));

        if (evaluationType != null) evaluation.setEvaluationType(evaluationType);
        if (evaluationPeriodStart != null) evaluation.setEvaluationPeriodStart(evaluationPeriodStart);
        if (evaluationPeriodEnd != null) evaluation.setEvaluationPeriodEnd(evaluationPeriodEnd);
        if (dueDate != null) evaluation.setDueDate(dueDate);
        if (status != null) evaluation.setStatus(status);
        if (overallRating != null) evaluation.setOverallRating(overallRating);
        if (performanceSummary != null) evaluation.setPerformanceSummary(performanceSummary);
        if (strengths != null) evaluation.setStrengths(strengths);
        if (areasForImprovement != null) evaluation.setAreasForImprovement(areasForImprovement);
        if (goalsAchieved != null) evaluation.setGoalsAchieved(goalsAchieved);
        if (goalsForNextPeriod != null) evaluation.setGoalsForNextPeriod(goalsForNextPeriod);
        
        // Update evaluator if provided
        if (evaluatorId != null) {
            employeeRepository.findById(evaluatorId).ifPresent(evaluation::setEvaluator);
        }
        
        if (evaluatorComments != null) evaluation.setEvaluatorComments(evaluatorComments);

        PerformanceEvaluationEntity updatedEvaluation = performanceEvaluationRepository.save(evaluation);
        return convertToDTO(updatedEvaluation);
    }

    @Transactional
    public PerformanceEvaluationDTO submitEvaluation(String evaluationId) {
        PerformanceEvaluationEntity evaluation = performanceEvaluationRepository.findById(evaluationId)
                .orElseThrow(() -> new RuntimeException("Performance evaluation not found"));

        evaluation.setStatus("SUBMITTED");
        evaluation.setSubmittedDate(LocalDate.now());

        PerformanceEvaluationEntity submittedEvaluation = performanceEvaluationRepository.save(evaluation);
        return convertToDTO(submittedEvaluation);
    }

    @Transactional
    public PerformanceEvaluationDTO addEmployeeComments(
            String evaluationId,
            String employeeComments,
            boolean acknowledge) {

        PerformanceEvaluationEntity evaluation = performanceEvaluationRepository.findById(evaluationId)
                .orElseThrow(() -> new RuntimeException("Performance evaluation not found"));

        evaluation.setEmployeeComments(employeeComments);
        
        if (acknowledge) {
            evaluation.setStatus("ACKNOWLEDGED");
            evaluation.setAcknowledgementDate(LocalDate.now());
        }

        PerformanceEvaluationEntity updatedEvaluation = performanceEvaluationRepository.save(evaluation);
        return convertToDTO(updatedEvaluation);
    }

    @Transactional
    public void deletePerformanceEvaluation(String evaluationId) {
        performanceEvaluationRepository.deleteById(evaluationId);
    }
}

// New file: Service for managing performance evaluations
// Handles creating, updating, and tracking employee performance evaluations 