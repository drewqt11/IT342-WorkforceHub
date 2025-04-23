package cit.edu.workforce.Service;

import cit.edu.workforce.DTO.CompanyLocationDTO;
import cit.edu.workforce.Entity.CompanyLocationEntity;
import cit.edu.workforce.Repository.CompanyLocationRepository;
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
public class CompanyLocationService {

    private final CompanyLocationRepository companyLocationRepository;

    @Autowired
    public CompanyLocationService(CompanyLocationRepository companyLocationRepository) {
        this.companyLocationRepository = companyLocationRepository;
    }

    @Transactional(readOnly = true)
    public List<CompanyLocationDTO> getAllActiveLocations() {
        return companyLocationRepository.findByIsActiveTrue().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<CompanyLocationDTO> getAllLocationsPaged(Pageable pageable) {
        return companyLocationRepository.findAll(pageable)
                .map(this::convertToDTO);
    }

    @Transactional(readOnly = true)
    public Page<CompanyLocationDTO> searchLocations(String locationName, Pageable pageable) {
        return companyLocationRepository.findByLocationNameContainingIgnoreCase(locationName, pageable)
                .map(this::convertToDTO);
    }

    @Transactional(readOnly = true)
    public Optional<CompanyLocationDTO> getLocationById(String id) {
        return companyLocationRepository.findById(id)
                .map(this::convertToDTO);
    }

    @Transactional
    public CompanyLocationDTO createLocation(CompanyLocationDTO locationDTO) {
        CompanyLocationEntity entity = convertToEntity(locationDTO);
        entity.setIsActive(true);
        return convertToDTO(companyLocationRepository.save(entity));
    }

    @Transactional
    public CompanyLocationDTO updateLocation(String id, CompanyLocationDTO locationDTO) {
        CompanyLocationEntity existingLocation = companyLocationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Company location not found with id: " + id));

        existingLocation.setLocationName(locationDTO.getLocationName());
        existingLocation.setAddress(locationDTO.getAddress());
        existingLocation.setLatitude(locationDTO.getLatitude());
        existingLocation.setLongitude(locationDTO.getLongitude());
        existingLocation.setAllowedRadius(locationDTO.getAllowedRadius());
        existingLocation.setNotes(locationDTO.getNotes());
        if (locationDTO.getIsActive() != null) {
            existingLocation.setIsActive(locationDTO.getIsActive());
        }

        return convertToDTO(companyLocationRepository.save(existingLocation));
    }

    @Transactional
    public CompanyLocationDTO deactivateLocation(String id) {
        CompanyLocationEntity location = companyLocationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Company location not found with id: " + id));
        
        location.setIsActive(false);
        return convertToDTO(companyLocationRepository.save(location));
    }

    @Transactional
    public CompanyLocationDTO activateLocation(String id) {
        CompanyLocationEntity location = companyLocationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Company location not found with id: " + id));
        
        location.setIsActive(true);
        return convertToDTO(companyLocationRepository.save(location));
    }

    @Transactional
    public void deleteLocation(String id) {
        if (!companyLocationRepository.existsById(id)) {
            throw new EntityNotFoundException("Company location not found with id: " + id);
        }
        companyLocationRepository.deleteById(id);
    }

    private CompanyLocationDTO convertToDTO(CompanyLocationEntity entity) {
        CompanyLocationDTO dto = new CompanyLocationDTO();
        dto.setId(entity.getId());
        dto.setLocationName(entity.getLocationName());
        dto.setAddress(entity.getAddress());
        dto.setLatitude(entity.getLatitude());
        dto.setLongitude(entity.getLongitude());
        dto.setAllowedRadius(entity.getAllowedRadius());
        dto.setNotes(entity.getNotes());
        dto.setIsActive(entity.getIsActive());
        return dto;
    }

    private CompanyLocationEntity convertToEntity(CompanyLocationDTO dto) {
        CompanyLocationEntity entity = new CompanyLocationEntity();
        // Don't set ID for new entities
        if (dto.getId() != null) {
            entity.setId(dto.getId());
        }
        entity.setLocationName(dto.getLocationName());
        entity.setAddress(dto.getAddress());
        entity.setLatitude(dto.getLatitude());
        entity.setLongitude(dto.getLongitude());
        entity.setAllowedRadius(dto.getAllowedRadius());
        entity.setNotes(dto.getNotes());
        entity.setIsActive(dto.getIsActive() != null ? dto.getIsActive() : true);
        return entity;
    }
} 