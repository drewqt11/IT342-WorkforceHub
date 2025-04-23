package cit.edu.workforce.Controller;

import cit.edu.workforce.DTO.CompanyLocationDTO;
import cit.edu.workforce.Service.CompanyLocationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api")
@Tag(name = "Company Locations", description = "APIs for managing company locations")
@SecurityRequirement(name = "bearerAuth")
public class CompanyLocationController {

    private final CompanyLocationService companyLocationService;

    @Autowired
    public CompanyLocationController(CompanyLocationService companyLocationService) {
        this.companyLocationService = companyLocationService;
    }

    @GetMapping("/locations")
    @Operation(summary = "Get all active locations", description = "Get a list of all active company locations")
    @PreAuthorize("hasAnyRole('ROLE_EMPLOYEE', 'ROLE_HR', 'ROLE_ADMIN')")
    public ResponseEntity<List<CompanyLocationDTO>> getAllActiveLocations() {
        return ResponseEntity.ok(companyLocationService.getAllActiveLocations());
    }

    @GetMapping("/hr/locations")
    @Operation(summary = "Get all locations (paged)", description = "Get a paginated list of all company locations")
    @PreAuthorize("hasAnyRole('ROLE_HR', 'ROLE_ADMIN')")
    public ResponseEntity<Page<CompanyLocationDTO>> getAllLocations(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "locationName") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "asc") String direction,
            @Parameter(description = "Filter by location name") @RequestParam(required = false) String locationName) {

        Sort sort = "desc".equalsIgnoreCase(direction) ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        if (locationName != null && !locationName.isEmpty()) {
            return ResponseEntity.ok(companyLocationService.searchLocations(locationName, pageable));
        } else {
            return ResponseEntity.ok(companyLocationService.getAllLocationsPaged(pageable));
        }
    }

    @GetMapping("/hr/locations/{id}")
    @Operation(summary = "Get location by ID", description = "Get a company location by its ID")
    @PreAuthorize("hasAnyRole('ROLE_HR', 'ROLE_ADMIN')")
    public ResponseEntity<CompanyLocationDTO> getLocationById(@PathVariable String id) {
        return companyLocationService.getLocationById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/hr/locations")
    @Operation(summary = "Create location", description = "Create a new company location")
    @PreAuthorize("hasAnyRole('ROLE_HR', 'ROLE_ADMIN')")
    public ResponseEntity<CompanyLocationDTO> createLocation(@Valid @RequestBody CompanyLocationDTO locationDTO) {
        return new ResponseEntity<>(companyLocationService.createLocation(locationDTO), HttpStatus.CREATED);
    }

    @PutMapping("/hr/locations/{id}")
    @Operation(summary = "Update location", description = "Update an existing company location")
    @PreAuthorize("hasAnyRole('ROLE_HR', 'ROLE_ADMIN')")
    public ResponseEntity<CompanyLocationDTO> updateLocation(
            @PathVariable String id,
            @Valid @RequestBody CompanyLocationDTO locationDTO) {
        try {
            return ResponseEntity.ok(companyLocationService.updateLocation(id, locationDTO));
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @PutMapping("/hr/locations/{id}/deactivate")
    @Operation(summary = "Deactivate location", description = "Deactivate a company location")
    @PreAuthorize("hasAnyRole('ROLE_HR', 'ROLE_ADMIN')")
    public ResponseEntity<CompanyLocationDTO> deactivateLocation(@PathVariable String id) {
        try {
            return ResponseEntity.ok(companyLocationService.deactivateLocation(id));
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @PutMapping("/hr/locations/{id}/activate")
    @Operation(summary = "Activate location", description = "Activate a company location")
    @PreAuthorize("hasAnyRole('ROLE_HR', 'ROLE_ADMIN')")
    public ResponseEntity<CompanyLocationDTO> activateLocation(@PathVariable String id) {
        try {
            return ResponseEntity.ok(companyLocationService.activateLocation(id));
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @DeleteMapping("/hr/locations/{id}")
    @Operation(summary = "Delete location", description = "Delete a company location")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN')")
    public ResponseEntity<Void> deleteLocation(@PathVariable String id) {
        try {
            companyLocationService.deleteLocation(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }
} 