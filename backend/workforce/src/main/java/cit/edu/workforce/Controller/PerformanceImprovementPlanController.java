package cit.edu.workforce.Controller;

import cit.edu.workforce.DTO.PerformanceImprovementPlanDTO;
import cit.edu.workforce.Service.PerformanceImprovementPlanService;
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
@RequestMapping("/api/performance/improvement-plans")
@Tag(name = "Performance Improvement Plan Management", description = "APIs for managing performance improvement plans")
@SecurityRequirement(name = "bearerAuth")
public class PerformanceImprovementPlanController {

    private final PerformanceImprovementPlanService pipService;

    @Autowired
    public PerformanceImprovementPlanController(PerformanceImprovementPlanService pipService) {
        this.pipService = pipService;
    }

    @GetMapping
    @Operation(summary = "Get all performance improvement plans", description = "Get a paginated list of all performance improvement plans")
    @PreAuthorize("hasAnyRole('ROLE_HR', 'ROLE_ADMIN')")
    public ResponseEntity<Page<PerformanceImprovementPlanDTO>> getAllPerformanceImprovementPlans(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "startDate") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "desc") String direction) {
        
        Sort.Direction sortDirection = direction.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));
        
        return ResponseEntity.ok(pipService.getAllPerformanceImprovementPlans(pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get performance improvement plan by ID", description = "Get a performance improvement plan by its ID")
    @PreAuthorize("hasAnyRole('ROLE_HR', 'ROLE_ADMIN', 'ROLE_EMPLOYEE')")
    public ResponseEntity<PerformanceImprovementPlanDTO> getPerformanceImprovementPlanById(@PathVariable String id) {
        return pipService.getPerformanceImprovementPlanById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/employee/{employeeId}")
    @Operation(summary = "Get plans by employee", description = "Get performance improvement plans for a specific employee")
    @PreAuthorize("hasAnyRole('ROLE_HR', 'ROLE_ADMIN', 'ROLE_EMPLOYEE')")
    public ResponseEntity<List<PerformanceImprovementPlanDTO>> getPlansByEmployee(@PathVariable String employeeId) {
        // For employee role, check if requesting own plans
        // This would typically be done with a custom method or in the service layer
        return ResponseEntity.ok(pipService.getPerformanceImprovementPlansByEmployee(employeeId));
    }

    @GetMapping("/manager/{managerId}")
    @Operation(summary = "Get plans by manager", description = "Get performance improvement plans assigned to a specific manager")
    @PreAuthorize("hasAnyRole('ROLE_HR', 'ROLE_ADMIN')")
    public ResponseEntity<List<PerformanceImprovementPlanDTO>> getPlansByManager(@PathVariable String managerId) {
        return ResponseEntity.ok(pipService.getPerformanceImprovementPlansByManager(managerId));
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Get plans by status", description = "Get performance improvement plans with a specific status")
    @PreAuthorize("hasAnyRole('ROLE_HR', 'ROLE_ADMIN')")
    public ResponseEntity<Page<PerformanceImprovementPlanDTO>> getPlansByStatus(
            @PathVariable String status,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(pipService.getPerformanceImprovementPlansByStatus(status, pageable));
    }

    @GetMapping("/active")
    @Operation(summary = "Get active plans", description = "Get active performance improvement plans")
    @PreAuthorize("hasAnyRole('ROLE_HR', 'ROLE_ADMIN')")
    public ResponseEntity<List<PerformanceImprovementPlanDTO>> getActivePlans() {
        return ResponseEntity.ok(pipService.getActivePerformanceImprovementPlans());
    }

    @PostMapping
    @Operation(summary = "Create performance improvement plan", description = "Create a new performance improvement plan")
    @PreAuthorize("hasAnyRole('ROLE_HR', 'ROLE_ADMIN')")
    public ResponseEntity<PerformanceImprovementPlanDTO> createPerformanceImprovementPlan(
            @Parameter(description = "Employee ID") @RequestParam String employeeId,
            @Parameter(description = "Start date") 
                @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "End date") 
                @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @Parameter(description = "Performance issues") @RequestParam String performanceIssues,
            @Parameter(description = "Improvement goals") @RequestParam String improvementGoals,
            @Parameter(description = "Action plan") @RequestParam String actionPlan,
            @Parameter(description = "Resources provided") @RequestParam(required = false) String resourcesProvided,
            @Parameter(description = "Evaluation criteria") @RequestParam(required = false) String evaluationCriteria,
            @Parameter(description = "Consequences") @RequestParam(required = false) String consequences,
            @Parameter(description = "Manager ID") @RequestParam String managerId,
            @Parameter(description = "HR representative ID") @RequestParam(required = false) String hrRepresentativeId) {

        PerformanceImprovementPlanDTO pip = pipService.createPerformanceImprovementPlan(
                employeeId, startDate, endDate, performanceIssues, improvementGoals, actionPlan,
                resourcesProvided, evaluationCriteria, consequences, managerId, hrRepresentativeId);
        
        return new ResponseEntity<>(pip, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update performance improvement plan", description = "Update an existing performance improvement plan")
    @PreAuthorize("hasAnyRole('ROLE_HR', 'ROLE_ADMIN')")
    public ResponseEntity<PerformanceImprovementPlanDTO> updatePerformanceImprovementPlan(
            @PathVariable String id,
            @Parameter(description = "Start date") 
                @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "End date") 
                @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @Parameter(description = "Status") @RequestParam(required = false) String status,
            @Parameter(description = "Performance issues") @RequestParam(required = false) String performanceIssues,
            @Parameter(description = "Improvement goals") @RequestParam(required = false) String improvementGoals,
            @Parameter(description = "Action plan") @RequestParam(required = false) String actionPlan,
            @Parameter(description = "Resources provided") @RequestParam(required = false) String resourcesProvided,
            @Parameter(description = "Evaluation criteria") @RequestParam(required = false) String evaluationCriteria,
            @Parameter(description = "Consequences") @RequestParam(required = false) String consequences,
            @Parameter(description = "Manager ID") @RequestParam(required = false) String managerId,
            @Parameter(description = "HR representative ID") @RequestParam(required = false) String hrRepresentativeId,
            @Parameter(description = "Progress notes") @RequestParam(required = false) String progressNotes) {

        PerformanceImprovementPlanDTO pip = pipService.updatePerformanceImprovementPlan(
                id, startDate, endDate, status, performanceIssues, improvementGoals, actionPlan,
                resourcesProvided, evaluationCriteria, consequences, managerId, hrRepresentativeId, progressNotes);
        
        return ResponseEntity.ok(pip);
    }

    @PutMapping("/{id}/complete")
    @Operation(summary = "Complete plan", description = "Mark a performance improvement plan as completed")
    @PreAuthorize("hasAnyRole('ROLE_HR', 'ROLE_ADMIN')")
    public ResponseEntity<PerformanceImprovementPlanDTO> completePlan(
            @PathVariable String id,
            @Parameter(description = "Final outcome") @RequestParam String finalOutcome,
            @Parameter(description = "Was the plan successful") @RequestParam boolean successful) {

        PerformanceImprovementPlanDTO pip = pipService.completePlan(id, finalOutcome, successful);
        return ResponseEntity.ok(pip);
    }

    @PutMapping("/{id}/terminate")
    @Operation(summary = "Terminate plan", description = "Terminate a performance improvement plan")
    @PreAuthorize("hasAnyRole('ROLE_HR', 'ROLE_ADMIN')")
    public ResponseEntity<PerformanceImprovementPlanDTO> terminatePlan(
            @PathVariable String id,
            @Parameter(description = "Final outcome") @RequestParam String finalOutcome) {

        PerformanceImprovementPlanDTO pip = pipService.terminatePlan(id, finalOutcome);
        return ResponseEntity.ok(pip);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete performance improvement plan", description = "Delete a performance improvement plan")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<Void> deletePerformanceImprovementPlan(@PathVariable String id) {
        pipService.deletePerformanceImprovementPlan(id);
        return ResponseEntity.noContent().build();
    }
}

// New file: Controller for managing performance improvement plans
// Provides API endpoints for creating, updating, and tracking employee performance improvement plans 