package cit.edu.workforce.Repository;

import cit.edu.workforce.Entity.CompanyLocationEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CompanyLocationRepository extends JpaRepository<CompanyLocationEntity, String> {
    
    List<CompanyLocationEntity> findByIsActiveTrue();
    
    Page<CompanyLocationEntity> findByIsActiveTrue(Pageable pageable);
    
    Page<CompanyLocationEntity> findByLocationNameContainingIgnoreCase(String name, Pageable pageable);
} 