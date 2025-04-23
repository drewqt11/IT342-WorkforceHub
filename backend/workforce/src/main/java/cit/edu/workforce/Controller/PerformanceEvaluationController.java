package cit.edu.workforce.Controller;

import cit.edu.workforce.DTO.PerformanceEvaluationDTO;
import cit.edu.workforce.Service.PerformanceEvaluationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/performance/evaluations")
@Tag(name = "Performance Evaluation Management", description = "APIs for managing performance evaluations")
@SecurityRequirement(name = "bearerAuth")
public class PerformanceEvaluationController {

    private final PerformanceEvaluationService performanceEvaluationService;

    @Autowired
    public PerformanceEvaluationController(PerformanceEvaluationService performanceEvaluationService) {
        this.performanceEvaluationService = performanceEvaluationService;
    }

    @GetMapping
    @Operation(summary = "Get all performance evaluations", description = "Get a paginated list of all performance evaluations")
    @PreAuthorize("hasAnyRole('ROLE_HR', 'ROLE_ADMIN')")
    public ResponseEntity<Page<PerformanceEvaluationDTO>> getAllPerformanceEvaluations(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "evaluationPeriodEnd") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "desc") String direction) {
        
        Sort.Direction sortDirection = direction.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));
        
        return ResponseEntity.ok(performanceEvaluationService.getAllPerformanceEvaluations(pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get performance evaluation by ID", description = "Get a performance evaluation by its ID")
    @PreAuthorize("hasAnyRole('ROLE_HR', 'ROLE_ADMIN', 'ROLE_EMPLOYEE')")
    public ResponseEntity<PerformanceEvaluationDTO> getPerformanceEvaluationById(@PathVariable String id) {
        return performanceEvaluationService.getPerformanceEvaluationById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/employee/{employeeId}")
    @Operation(summary = "Get evaluations by employee", description = "Get performance evaluations for a specific employee")
    @PreAuthorize("hasAnyRole('ROLE_HR', 'ROLE_ADMIN', 'ROLE_EMPLOYEE')")
    public ResponseEntity<List<PerformanceEvaluationDTO>> getEvaluationsByEmployee(@PathVariable String employeeId) {
        // For employee role, check if requesting own evaluations
        // This would typically be done with a custom method or in the service layer
        return ResponseEntity.ok(performanceEvaluationService.getPerformanceEvaluationsByEmployee(employeeId));
    }

    @GetMapping("/evaluator/{evaluatorId}")
    @Operation(summary = "Get evaluations by evaluator", description = "Get performance evaluations assigned to a specific evaluator")
    @PreAuthorize("hasAnyRole('ROLE_HR', 'ROLE_ADMIN')")
    public ResponseEntity<List<PerformanceEvaluationDTO>> getEvaluationsByEvaluator(@PathVariable String evaluatorId) {
        return ResponseEntity.ok(performanceEvaluationService.getPerformanceEvaluationsByEvaluator(evaluatorId));
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Get evaluations by status", description = "Get performance evaluations with a specific status")
    @PreAuthorize("hasAnyRole('ROLE_HR', 'ROLE_ADMIN')")
    public ResponseEntity<Page<PerformanceEvaluationDTO>> getEvaluationsByStatus(
            @PathVariable String status,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(performanceEvaluationService.getPerformanceEvaluationsByStatus(status, pageable));
    }

    @GetMapping("/type/{type}")
    @Operation(summary = "Get evaluations by type", description = "Get performance evaluations with a specific type")
    @PreAuthorize("hasAnyRole('ROLE_HR', 'ROLE_ADMIN')")
    public ResponseEntity<Page<PerformanceEvaluationDTO>> getEvaluationsByType(
            @PathVariable String type,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(performanceEvaluationService.getPerformanceEvaluationsByType(type, pageable));
    }

    @GetMapping("/pending")
    @Operation(summary = "Get pending evaluations", description = "Get performance evaluations that are pending and past their due date")
    @PreAuthorize("hasAnyRole('ROLE_HR', 'ROLE_ADMIN')")
    public ResponseEntity<List<PerformanceEvaluationDTO>> getPendingEvaluations() {
        return ResponseEntity.ok(performanceEvaluationService.getPendingEvaluations());
    }

    @PostMapping
    @Operation(summary = "Create performance evaluation", description = "Create a new performance evaluation")
    @PreAuthorize("hasAnyRole('ROLE_HR', 'ROLE_ADMIN')")
    public ResponseEntity<PerformanceEvaluationDTO> createPerformanceEvaluation(
            @Parameter(description = "Employee ID") @RequestParam String employeeId,
            @Parameter(description = "Evaluation type (ANNUAL, QUARTERLY, PROBATION, SELF, PEER)") 
                @RequestParam String evaluationType,
            @Parameter(description = "Evaluation period start date") 
                @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate evaluationPeriodStart,
            @Parameter(description = "Evaluation period end date") 
                @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate evaluationPeriodEnd,
            @Parameter(description = "Due date") 
                @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dueDate,
            @Parameter(description = "Evaluator ID") @RequestParam(required = false) String evaluatorId) {

        PerformanceEvaluationDTO evaluation = performanceEvaluationService.createPerformanceEvaluation(
                employeeId, evaluationType, evaluationPeriodStart, evaluationPeriodEnd, dueDate, evaluatorId);
        
        return new ResponseEntity<>(evaluation, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update performance evaluation", description = "Update an existing performance evaluation")
    @PreAuthorize("hasAnyRole('ROLE_HR', 'ROLE_ADMIN')")
    public ResponseEntity<PerformanceEvaluationDTO> updatePerformanceEvaluation(
            @PathVariable String id,
            @Parameter(description = "Evaluation type") @RequestParam(required = false) String evaluationType,
            @Parameter(description = "Evaluation period start date") 
                @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate evaluationPeriodStart,
            @Parameter(description = "Evaluation period end date") 
                @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate evaluationPeriodEnd,
            @Parameter(description = "Due date") 
                @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dueDate,
            @Parameter(description = "Status") @RequestParam(required = false) String status,
            @Parameter(description = "Overall rating (1-5)") @RequestParam(required = false) Integer overallRating,
            @Parameter(description = "Performance summary") @RequestParam(required = false) String performanceSummary,
            @Parameter(description = "Strengths") @RequestParam(required = false) String strengths,
            @Parameter(description = "Areas for improvement") @RequestParam(required = false) String areasForImprovement,
            @Parameter(description = "Goals achieved") @RequestParam(required = false) String goalsAchieved,
            @Parameter(description = "Goals for next period") @RequestParam(required = false) String goalsForNextPeriod,
            @Parameter(description = "Evaluator ID") @RequestParam(required = false) String evaluatorId,
            @Parameter(description = "Evaluator comments") @RequestParam(required = false) String evaluatorComments) {

        PerformanceEvaluationDTO evaluation = performanceEvaluationService.updatePerformanceEvaluation(
                id, evaluationType, evaluationPeriodStart, evaluationPeriodEnd, dueDate, status, overallRating,
                performanceSummary, strengths, areasForImprovement, goalsAchieved, goalsForNextPeriod,
                evaluatorId, evaluatorComments);
        
        return ResponseEntity.ok(evaluation);
    }

    @PutMapping("/{id}/submit")
    @Operation(summary = "Submit evaluation", description = "Submit a completed evaluation")
    @PreAuthorize("hasAnyRole('ROLE_HR', 'ROLE_ADMIN')")
    public ResponseEntity<PerformanceEvaluationDTO> submitEvaluation(@PathVariable String id) {
        PerformanceEvaluationDTO evaluation = performanceEvaluationService.submitEvaluation(id);
        return ResponseEntity.ok(evaluation);
    }

    @PutMapping("/{id}/employee-comments")
    @Operation(summary = "Add employee comments", description = "Add employee comments to an evaluation")
    @PreAuthorize("hasAnyRole('ROLE_HR', 'ROLE_ADMIN', 'ROLE_EMPLOYEE')")
    public ResponseEntity<PerformanceEvaluationDTO> addEmployeeComments(
            @PathVariable String id,
            @Parameter(description = "Employee comments") @RequestParam String employeeComments,
            @Parameter(description = "Acknowledge evaluation") @RequestParam(defaultValue = "false") boolean acknowledge) {

        PerformanceEvaluationDTO evaluation = performanceEvaluationService.addEmployeeComments(
                id, employeeComments, acknowledge);
        
        return ResponseEntity.ok(evaluation);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete performance evaluation", description = "Delete a performance evaluation")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<Void> deletePerformanceEvaluation(@PathVariable String id) {
        performanceEvaluationService.deletePerformanceEvaluation(id);
        return ResponseEntity.noContent().build();
    }
}

// New file: Controller for managing performance evaluations
// Provides API endpoints for creating, updating, and tracking employee performance evaluations 