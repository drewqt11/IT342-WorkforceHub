package cit.edu.workforce.Service;

import cit.edu.workforce.DTO.LeaveTypeDTO;
import cit.edu.workforce.Entity.LeaveTypeEntity;
import cit.edu.workforce.Repository.LeaveTypeRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class LeaveTypeService {

    private final LeaveTypeRepository leaveTypeRepository;

    @Autowired
    public LeaveTypeService(LeaveTypeRepository leaveTypeRepository) {
        this.leaveTypeRepository = leaveTypeRepository;
    }

    @Transactional(readOnly = true)
    public List<LeaveTypeDTO> getAllActiveLeaveTypes() {
        return leaveTypeRepository.findByIsActiveTrue().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<LeaveTypeDTO> getAllLeaveTypesPaged(Pageable pageable) {
        return leaveTypeRepository.findAll(pageable)
                .map(this::convertToDTO);
    }

    @Transactional(readOnly = true)
    public Page<LeaveTypeDTO> searchLeaveTypes(String name, Pageable pageable) {
        return leaveTypeRepository.findByNameContainingIgnoreCase(name, pageable)
                .map(this::convertToDTO);
    }

    @Transactional(readOnly = true)
    public Optional<LeaveTypeDTO> getLeaveTypeById(String id) {
        return leaveTypeRepository.findById(id)
                .map(this::convertToDTO);
    }

    @Transactional
    public LeaveTypeDTO createLeaveType(LeaveTypeDTO leaveTypeDTO) {
        // Validate uniqueness of name
        if (leaveTypeRepository.existsByName(leaveTypeDTO.getName())) {
            throw new IllegalArgumentException("Leave type with name '" + leaveTypeDTO.getName() + "' already exists");
        }

        LeaveTypeEntity entity = convertToEntity(leaveTypeDTO);
        entity.setIsActive(true);
        return convertToDTO(leaveTypeRepository.save(entity));
    }

    @Transactional
    public LeaveTypeDTO updateLeaveType(String id, LeaveTypeDTO leaveTypeDTO) {
        LeaveTypeEntity existingLeaveType = leaveTypeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Leave type not found with id: " + id));

        // Validate uniqueness of name if changed
        if (!existingLeaveType.getName().equals(leaveTypeDTO.getName()) &&
                leaveTypeRepository.existsByName(leaveTypeDTO.getName())) {
            throw new IllegalArgumentException("Leave type with name '" + leaveTypeDTO.getName() + "' already exists");
        }

        existingLeaveType.setName(leaveTypeDTO.getName());
        existingLeaveType.setDescription(leaveTypeDTO.getDescription());
        existingLeaveType.setDefaultDays(leaveTypeDTO.getDefaultDays());
        existingLeaveType.setIsPaid(leaveTypeDTO.getIsPaid());
        existingLeaveType.setRequiresApproval(leaveTypeDTO.getRequiresApproval());
        
        if (leaveTypeDTO.getIsActive() != null) {
            existingLeaveType.setIsActive(leaveTypeDTO.getIsActive());
        }

        return convertToDTO(leaveTypeRepository.save(existingLeaveType));
    }

    @Transactional
    public LeaveTypeDTO deactivateLeaveType(String id) {
        LeaveTypeEntity leaveType = leaveTypeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Leave type not found with id: " + id));
        
        leaveType.setIsActive(false);
        return convertToDTO(leaveTypeRepository.save(leaveType));
    }

    @Transactional
    public LeaveTypeDTO activateLeaveType(String id) {
        LeaveTypeEntity leaveType = leaveTypeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Leave type not found with id: " + id));
        
        leaveType.setIsActive(true);
        return convertToDTO(leaveTypeRepository.save(leaveType));
    }

    @Transactional
    public void deleteLeaveType(String id) {
        if (!leaveTypeRepository.existsById(id)) {
            throw new EntityNotFoundException("Leave type not found with id: " + id);
        }
        leaveTypeRepository.deleteById(id);
    }

    private LeaveTypeDTO convertToDTO(LeaveTypeEntity entity) {
        LeaveTypeDTO dto = new LeaveTypeDTO();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setDescription(entity.getDescription());
        dto.setDefaultDays(entity.getDefaultDays());
        dto.setIsPaid(entity.getIsPaid());
        dto.setRequiresApproval(entity.getRequiresApproval());
        dto.setIsActive(entity.getIsActive());
        return dto;
    }

    private LeaveTypeEntity convertToEntity(LeaveTypeDTO dto) {
        LeaveTypeEntity entity = new LeaveTypeEntity();
        // Don't set ID for new entities
        if (dto.getId() != null) {
            entity.setId(dto.getId());
        }
        entity.setName(dto.getName());
        entity.setDescription(dto.getDescription());
        entity.setDefaultDays(dto.getDefaultDays());
        entity.setIsPaid(dto.getIsPaid() != null ? dto.getIsPaid() : true);
        entity.setRequiresApproval(dto.getRequiresApproval() != null ? dto.getRequiresApproval() : true);
        entity.setIsActive(dto.getIsActive() != null ? dto.getIsActive() : true);
        return entity;
    }
}

// New file: Service for leave types management with CRUD operations 