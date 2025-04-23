package cit.edu.workforce.Service;

import cit.edu.workforce.DTO.PerformanceImprovementPlanDTO;
import cit.edu.workforce.Entity.EmployeeEntity;
import cit.edu.workforce.Entity.PerformanceImprovementPlanEntity;
import cit.edu.workforce.Repository.EmployeeRepository;
import cit.edu.workforce.Repository.PerformanceImprovementPlanRepository;
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
public class PerformanceImprovementPlanService {

    private final PerformanceImprovementPlanRepository pipRepository;
    private final EmployeeRepository employeeRepository;

    @Autowired
    public PerformanceImprovementPlanService(
            PerformanceImprovementPlanRepository pipRepository,
            EmployeeRepository employeeRepository) {
        this.pipRepository = pipRepository;
        this.employeeRepository = employeeRepository;
    }

    // Convert entity to DTO
    public PerformanceImprovementPlanDTO convertToDTO(PerformanceImprovementPlanEntity pip) {
        PerformanceImprovementPlanDTO dto = new PerformanceImprovementPlanDTO();
        dto.setPipId(pip.getPipId());
        
        if (pip.getEmployee() != null) {
            dto.setEmployeeId(pip.getEmployee().getEmployeeId());
            dto.setEmployeeName(pip.getEmployee().getFirstName() + " " + pip.getEmployee().getLastName());
        }
        
        dto.setStartDate(pip.getStartDate());
        dto.setEndDate(pip.getEndDate());
        dto.setStatus(pip.getStatus());
        dto.setPerformanceIssues(pip.getPerformanceIssues());
        dto.setImprovementGoals(pip.getImprovementGoals());
        dto.setActionPlan(pip.getActionPlan());
        dto.setResourcesProvided(pip.getResourcesProvided());
        dto.setEvaluationCriteria(pip.getEvaluationCriteria());
        dto.setConsequences(pip.getConsequences());
        
        if (pip.getManager() != null) {
            dto.setManagerId(pip.getManager().getEmployeeId());
            dto.setManagerName(pip.getManager().getFirstName() + " " + pip.getManager().getLastName());
        }
        
        if (pip.getHrRepresentative() != null) {
            dto.setHrRepresentativeId(pip.getHrRepresentative().getEmployeeId());
            dto.setHrRepresentativeName(pip.getHrRepresentative().getFirstName() + " " + pip.getHrRepresentative().getLastName());
        }
        
        dto.setProgressNotes(pip.getProgressNotes());
        dto.setFinalOutcome(pip.getFinalOutcome());
        dto.setCompletionDate(pip.getCompletionDate());
        
        return dto;
    }

    @Transactional(readOnly = true)
    public List<PerformanceImprovementPlanDTO> getAllPerformanceImprovementPlans() {
        return pipRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<PerformanceImprovementPlanDTO> getAllPerformanceImprovementPlans(Pageable pageable) {
        return pipRepository.findAll(pageable)
                .map(this::convertToDTO);
    }

    @Transactional(readOnly = true)
    public Optional<PerformanceImprovementPlanDTO> getPerformanceImprovementPlanById(String pipId) {
        return pipRepository.findById(pipId)
                .map(this::convertToDTO);
    }

    @Transactional(readOnly = true)
    public List<PerformanceImprovementPlanDTO> getPerformanceImprovementPlansByEmployee(String employeeId) {
        Optional<EmployeeEntity> employee = employeeRepository.findById(employeeId);
        if (employee.isPresent()) {
            return pipRepository.findByEmployee(employee.get()).stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
        }
        return List.of();
    }

    @Transactional(readOnly = true)
    public Page<PerformanceImprovementPlanDTO> getPerformanceImprovementPlansByEmployee(String employeeId, Pageable pageable) {
        Optional<EmployeeEntity> employee = employeeRepository.findById(employeeId);
        if (employee.isPresent()) {
            return pipRepository.findByEmployee(employee.get(), pageable)
                    .map(this::convertToDTO);
        }
        return Page.empty();
    }

    @Transactional(readOnly = true)
    public List<PerformanceImprovementPlanDTO> getPerformanceImprovementPlansByManager(String managerId) {
        Optional<EmployeeEntity> manager = employeeRepository.findById(managerId);
        if (manager.isPresent()) {
            return pipRepository.findByManager(manager.get()).stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
        }
        return List.of();
    }

    @Transactional(readOnly = true)
    public Page<PerformanceImprovementPlanDTO> getPerformanceImprovementPlansByManager(String managerId, Pageable pageable) {
        Optional<EmployeeEntity> manager = employeeRepository.findById(managerId);
        if (manager.isPresent()) {
            return pipRepository.findByManager(manager.get(), pageable)
                    .map(this::convertToDTO);
        }
        return Page.empty();
    }

    @Transactional(readOnly = true)
    public List<PerformanceImprovementPlanDTO> getPerformanceImprovementPlansByStatus(String status) {
        return pipRepository.findByStatus(status).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<PerformanceImprovementPlanDTO> getPerformanceImprovementPlansByStatus(String status, Pageable pageable) {
        return pipRepository.findByStatus(status, pageable)
                .map(this::convertToDTO);
    }

    @Transactional(readOnly = true)
    public List<PerformanceImprovementPlanDTO> getActivePerformanceImprovementPlans() {
        LocalDate currentDate = LocalDate.now();
        return pipRepository
                .findByStartDateLessThanEqualAndEndDateGreaterThanEqual(currentDate, currentDate).stream()
                .filter(pip -> pip.getStatus().equals("ACTIVE"))
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public PerformanceImprovementPlanDTO createPerformanceImprovementPlan(
            String employeeId,
            LocalDate startDate,
            LocalDate endDate,
            String performanceIssues,
            String improvementGoals,
            String actionPlan,
            String resourcesProvided,
            String evaluationCriteria,
            String consequences,
            String managerId,
            String hrRepresentativeId) {

        // Check if employee exists
        EmployeeEntity employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee not found"));
        
        // Check if manager exists
        EmployeeEntity manager = employeeRepository.findById(managerId)
                .orElseThrow(() -> new RuntimeException("Manager not found"));

        PerformanceImprovementPlanEntity pip = new PerformanceImprovementPlanEntity();
        pip.setEmployee(employee);
        pip.setStartDate(startDate);
        pip.setEndDate(endDate);
        pip.setStatus("ACTIVE");
        pip.setPerformanceIssues(performanceIssues);
        pip.setImprovementGoals(improvementGoals);
        pip.setActionPlan(actionPlan);
        pip.setResourcesProvided(resourcesProvided);
        pip.setEvaluationCriteria(evaluationCriteria);
        pip.setConsequences(consequences);
        pip.setManager(manager);
        
        // Set HR representative if provided
        if (hrRepresentativeId != null) {
            employeeRepository.findById(hrRepresentativeId).ifPresent(pip::setHrRepresentative);
        }

        PerformanceImprovementPlanEntity savedPip = pipRepository.save(pip);
        return convertToDTO(savedPip);
    }

    @Transactional
    public PerformanceImprovementPlanDTO updatePerformanceImprovementPlan(
            String pipId,
            LocalDate startDate,
            LocalDate endDate,
            String status,
            String performanceIssues,
            String improvementGoals,
            String actionPlan,
            String resourcesProvided,
            String evaluationCriteria,
            String consequences,
            String managerId,
            String hrRepresentativeId,
            String progressNotes) {

        PerformanceImprovementPlanEntity pip = pipRepository.findById(pipId)
                .orElseThrow(() -> new RuntimeException("Performance improvement plan not found"));

        if (startDate != null) pip.setStartDate(startDate);
        if (endDate != null) pip.setEndDate(endDate);
        if (status != null) pip.setStatus(status);
        if (performanceIssues != null) pip.setPerformanceIssues(performanceIssues);
        if (improvementGoals != null) pip.setImprovementGoals(improvementGoals);
        if (actionPlan != null) pip.setActionPlan(actionPlan);
        if (resourcesProvided != null) pip.setResourcesProvided(resourcesProvided);
        if (evaluationCriteria != null) pip.setEvaluationCriteria(evaluationCriteria);
        if (consequences != null) pip.setConsequences(consequences);
        
        // Update manager if provided
        if (managerId != null) {
            employeeRepository.findById(managerId).ifPresent(pip::setManager);
        }
        
        // Update HR representative if provided
        if (hrRepresentativeId != null) {
            employeeRepository.findById(hrRepresentativeId).ifPresent(pip::setHrRepresentative);
        }
        
        // Append progress notes if provided
        if (progressNotes != null) {
            String existingNotes = pip.getProgressNotes();
            String datePrefix = LocalDate.now().toString() + ": ";
            String newNote = datePrefix + progressNotes;
            
            if (existingNotes != null && !existingNotes.isEmpty()) {
                pip.setProgressNotes(existingNotes + "\n\n" + newNote);
            } else {
                pip.setProgressNotes(newNote);
            }
        }

        PerformanceImprovementPlanEntity updatedPip = pipRepository.save(pip);
        return convertToDTO(updatedPip);
    }

    @Transactional
    public PerformanceImprovementPlanDTO completePlan(
            String pipId,
            String finalOutcome,
            boolean successful) {

        PerformanceImprovementPlanEntity pip = pipRepository.findById(pipId)
                .orElseThrow(() -> new RuntimeException("Performance improvement plan not found"));

        pip.setStatus("COMPLETED");
        pip.setFinalOutcome(finalOutcome);
        pip.setCompletionDate(LocalDate.now());

        PerformanceImprovementPlanEntity completedPip = pipRepository.save(pip);
        return convertToDTO(completedPip);
    }

    @Transactional
    public PerformanceImprovementPlanDTO terminatePlan(
            String pipId,
            String finalOutcome) {

        PerformanceImprovementPlanEntity pip = pipRepository.findById(pipId)
                .orElseThrow(() -> new RuntimeException("Performance improvement plan not found"));

        pip.setStatus("TERMINATED");
        pip.setFinalOutcome(finalOutcome);
        pip.setCompletionDate(LocalDate.now());

        PerformanceImprovementPlanEntity terminatedPip = pipRepository.save(pip);
        return convertToDTO(terminatedPip);
    }

    @Transactional
    public void deletePerformanceImprovementPlan(String pipId) {
        pipRepository.deleteById(pipId);
    }
}

// New file: Service for managing performance improvement plans
// Handles creating, updating, and tracking employee performance improvement plans 