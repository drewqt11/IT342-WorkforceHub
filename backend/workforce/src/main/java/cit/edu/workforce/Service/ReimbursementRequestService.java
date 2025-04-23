package cit.edu.workforce.Service;

import cit.edu.workforce.DTO.ReimbursementRequestDTO;
import cit.edu.workforce.Entity.EmployeeEntity;
import cit.edu.workforce.Entity.ReimbursementRequestEntity;
import cit.edu.workforce.Repository.EmployeeRepository;
import cit.edu.workforce.Repository.ReimbursementRequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ReimbursementRequestService {

    private final ReimbursementRequestRepository reimbursementRequestRepository;
    private final EmployeeRepository employeeRepository;

    @Autowired
    public ReimbursementRequestService(
            ReimbursementRequestRepository reimbursementRequestRepository,
            EmployeeRepository employeeRepository) {
        this.reimbursementRequestRepository = reimbursementRequestRepository;
        this.employeeRepository = employeeRepository;
    }

    @Transactional(readOnly = true)
    public List<ReimbursementRequestDTO> getAllReimbursementRequests() {
        return reimbursementRequestRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<ReimbursementRequestDTO> getAllReimbursementRequestsPaged(Pageable pageable) {
        return reimbursementRequestRepository.findAll(pageable)
                .map(this::convertToDTO);
    }

    @Transactional(readOnly = true)
    public List<ReimbursementRequestDTO> getCurrentEmployeeReimbursementRequests() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        
        EmployeeEntity employee = employeeRepository.findByEmail(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee not found"));
        
        return reimbursementRequestRepository.findByEmployee(employee).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<ReimbursementRequestDTO> getCurrentEmployeeReimbursementRequestsPaged(Pageable pageable) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        
        EmployeeEntity employee = employeeRepository.findByEmail(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee not found"));
        
        return reimbursementRequestRepository.findByEmployee(employee, pageable)
                .map(this::convertToDTO);
    }

    @Transactional(readOnly = true)
    public List<ReimbursementRequestDTO> getEmployeeReimbursementRequests(String employeeId) {
        EmployeeEntity employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, 
                        "Employee not found with ID: " + employeeId));
        
        return reimbursementRequestRepository.findByEmployee(employee).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<ReimbursementRequestDTO> getEmployeeReimbursementRequestsPaged(String employeeId, Pageable pageable) {
        EmployeeEntity employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, 
                        "Employee not found with ID: " + employeeId));
        
        return reimbursementRequestRepository.findByEmployee(employee, pageable)
                .map(this::convertToDTO);
    }

    @Transactional(readOnly = true)
    public List<ReimbursementRequestDTO> getReimbursementRequestsByStatus(String status) {
        return reimbursementRequestRepository.findByStatus(status).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<ReimbursementRequestDTO> getReimbursementRequestsByStatusPaged(String status, Pageable pageable) {
        return reimbursementRequestRepository.findByStatus(status, pageable)
                .map(this::convertToDTO);
    }

    @Transactional(readOnly = true)
    public List<ReimbursementRequestDTO> getReimbursementRequestsByExpenseType(String expenseType) {
        return reimbursementRequestRepository.findByExpenseType(expenseType).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<ReimbursementRequestDTO> getReimbursementRequestsByExpenseTypePaged(String expenseType, Pageable pageable) {
        return reimbursementRequestRepository.findByExpenseType(expenseType, pageable)
                .map(this::convertToDTO);
    }

    @Transactional(readOnly = true)
    public Optional<ReimbursementRequestDTO> getReimbursementRequestById(String requestId) {
        return reimbursementRequestRepository.findById(requestId)
                .map(this::convertToDTO);
    }

    @Transactional
    public ReimbursementRequestDTO createReimbursementRequest(ReimbursementRequestDTO requestDTO) {
        // For current employee if no employee ID provided
        final String employeeIdFromDTO = requestDTO.getEmployeeId();
        String employeeId = employeeIdFromDTO;
        
        if (employeeId == null || employeeId.isEmpty()) {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            
            EmployeeEntity employee = employeeRepository.findByEmail(username)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee not found"));
            employeeId = employee.getEmployeeId();
        }
        
        final String finalEmployeeId = employeeId;
        EmployeeEntity employee = employeeRepository.findById(finalEmployeeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, 
                        "Employee not found with ID: " + finalEmployeeId));
        
        ReimbursementRequestEntity request = new ReimbursementRequestEntity();
        request.setEmployee(employee);
        request.setExpenseDate(requestDTO.getExpenseDate());
        request.setExpenseType(requestDTO.getExpenseType());
        request.setAmount(requestDTO.getAmount());
        request.setCurrency(requestDTO.getCurrency() != null ? requestDTO.getCurrency() : "USD");
        request.setDescription(requestDTO.getDescription());
        request.setReceiptUrl(requestDTO.getReceiptUrl());
        request.setStatus("PENDING"); // Default status for new requests
        
        ReimbursementRequestEntity savedRequest = reimbursementRequestRepository.save(request);
        return convertToDTO(savedRequest);
    }

    @Transactional
    public ReimbursementRequestDTO approveReimbursementRequest(String requestId, String approverId, String comments) {
        ReimbursementRequestEntity request = reimbursementRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, 
                        "Reimbursement request not found with ID: " + requestId));
        
        if (!"PENDING".equals(request.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                    "Only pending requests can be approved");
        }
        
        EmployeeEntity approver = employeeRepository.findById(approverId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, 
                        "Approver not found with ID: " + approverId));
        
        request.setApprover(approver);
        request.setApproverComments(comments);
        request.setStatus("APPROVED");
        request.setApprovalDate(LocalDate.now());
        
        ReimbursementRequestEntity updatedRequest = reimbursementRequestRepository.save(request);
        return convertToDTO(updatedRequest);
    }

    @Transactional
    public ReimbursementRequestDTO rejectReimbursementRequest(String requestId, String approverId, String comments) {
        ReimbursementRequestEntity request = reimbursementRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, 
                        "Reimbursement request not found with ID: " + requestId));
        
        if (!"PENDING".equals(request.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                    "Only pending requests can be rejected");
        }
        
        EmployeeEntity approver = employeeRepository.findById(approverId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, 
                        "Approver not found with ID: " + approverId));
        
        request.setApprover(approver);
        request.setApproverComments(comments);
        request.setStatus("REJECTED");
        request.setApprovalDate(LocalDate.now());
        
        ReimbursementRequestEntity updatedRequest = reimbursementRequestRepository.save(request);
        return convertToDTO(updatedRequest);
    }

    @Transactional
    public ReimbursementRequestDTO markAsPaid(String requestId, String paymentReference) {
        ReimbursementRequestEntity request = reimbursementRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, 
                        "Reimbursement request not found with ID: " + requestId));
        
        if (!"APPROVED".equals(request.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                    "Only approved requests can be marked as paid");
        }
        
        request.setStatus("PAID");
        request.setPaymentDate(LocalDate.now());
        request.setPaymentReference(paymentReference);
        
        ReimbursementRequestEntity updatedRequest = reimbursementRequestRepository.save(request);
        return convertToDTO(updatedRequest);
    }

    @Transactional
    public ReimbursementRequestDTO updateReimbursementRequest(String requestId, ReimbursementRequestDTO requestDTO) {
        ReimbursementRequestEntity existingRequest = reimbursementRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, 
                        "Reimbursement request not found with ID: " + requestId));
        
        // Only allow updates for pending requests
        if (!"PENDING".equals(existingRequest.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                    "Only pending requests can be updated");
        }
        
        if (requestDTO.getExpenseDate() != null) {
            existingRequest.setExpenseDate(requestDTO.getExpenseDate());
        }
        
        if (requestDTO.getExpenseType() != null) {
            existingRequest.setExpenseType(requestDTO.getExpenseType());
        }
        
        if (requestDTO.getAmount() != null) {
            existingRequest.setAmount(requestDTO.getAmount());
        }
        
        if (requestDTO.getCurrency() != null) {
            existingRequest.setCurrency(requestDTO.getCurrency());
        }
        
        if (requestDTO.getDescription() != null) {
            existingRequest.setDescription(requestDTO.getDescription());
        }
        
        if (requestDTO.getReceiptUrl() != null) {
            existingRequest.setReceiptUrl(requestDTO.getReceiptUrl());
        }
        
        ReimbursementRequestEntity updatedRequest = reimbursementRequestRepository.save(existingRequest);
        return convertToDTO(updatedRequest);
    }

    @Transactional
    public void deleteReimbursementRequest(String requestId) {
        ReimbursementRequestEntity request = reimbursementRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, 
                        "Reimbursement request not found with ID: " + requestId));
        
        // Only allow deletion of pending requests
        if (!"PENDING".equals(request.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                    "Only pending requests can be deleted");
        }
        
        reimbursementRequestRepository.delete(request);
    }

    // Helper methods
    private ReimbursementRequestDTO convertToDTO(ReimbursementRequestEntity request) {
        ReimbursementRequestDTO dto = new ReimbursementRequestDTO();
        dto.setRequestId(request.getRequestId());
        dto.setEmployeeId(request.getEmployee().getEmployeeId());
        dto.setEmployeeName(request.getEmployee().getFirstName() + " " + request.getEmployee().getLastName());
        dto.setRequestDate(request.getRequestDate());
        dto.setExpenseDate(request.getExpenseDate());
        dto.setExpenseType(request.getExpenseType());
        dto.setAmount(request.getAmount());
        dto.setCurrency(request.getCurrency());
        dto.setDescription(request.getDescription());
        dto.setReceiptUrl(request.getReceiptUrl());
        dto.setStatus(request.getStatus());
        dto.setApprovalDate(request.getApprovalDate());
        
        if (request.getApprover() != null) {
            dto.setApproverId(request.getApprover().getEmployeeId());
            dto.setApproverName(request.getApprover().getFirstName() + " " + request.getApprover().getLastName());
        }
        
        dto.setApproverComments(request.getApproverComments());
        dto.setPaymentDate(request.getPaymentDate());
        dto.setPaymentReference(request.getPaymentReference());
        
        return dto;
    }
}

// New file: Service for reimbursement requests in the Benefits Administration module 