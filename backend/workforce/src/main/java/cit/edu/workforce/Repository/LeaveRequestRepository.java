package cit.edu.workforce.Repository;

import cit.edu.workforce.Entity.LeaveRequestEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface LeaveRequestRepository extends JpaRepository<LeaveRequestEntity, Long> {
    List<LeaveRequestEntity> findByEmployeeId(Long employeeId);
    List<LeaveRequestEntity> findByStatus(String status);
} 