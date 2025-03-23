package cit.edu.workforce.Security;

import cit.edu.workforce.Entity.EmployeeEntity;
import cit.edu.workforce.Entity.LeaveRequestEntity;
import cit.edu.workforce.Entity.NotificationEntity;
import cit.edu.workforce.Repository.EmployeeRepository;
import cit.edu.workforce.Repository.LeaveRequestRepository;
import cit.edu.workforce.Repository.NotificationRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class SecurityService {
    
    private final EmployeeRepository employeeRepository;
    private final LeaveRequestRepository leaveRequestRepository;
    private final NotificationRepository notificationRepository;
    
    public SecurityService(EmployeeRepository employeeRepository, 
                          LeaveRequestRepository leaveRequestRepository, 
                          NotificationRepository notificationRepository) {
        this.employeeRepository = employeeRepository;
        this.leaveRequestRepository = leaveRequestRepository;
        this.notificationRepository = notificationRepository;
    }
    
    public boolean isUserOwner(Authentication authentication, Long userId) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        Optional<EmployeeEntity> employeeOptional = employeeRepository.findByUserId(userId);
        
        if (employeeOptional.isPresent()) {
            EmployeeEntity employee = employeeOptional.get();
            return employee.getUser().getUsername().equals(userDetails.getUsername());
        }
        
        return false;
    }
    
    public boolean isEmployeeOwner(Authentication authentication, Long employeeId) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        Optional<EmployeeEntity> employeeOptional = employeeRepository.findById(employeeId);
        
        if (employeeOptional.isPresent()) {
            EmployeeEntity employee = employeeOptional.get();
            return employee.getUser().getUsername().equals(userDetails.getUsername());
        }
        
        return false;
    }
    
    public boolean isLeaveRequestOwner(Authentication authentication, Long leaveRequestId) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        Optional<LeaveRequestEntity> leaveRequestOptional = leaveRequestRepository.findById(leaveRequestId);
        
        if (leaveRequestOptional.isPresent()) {
            LeaveRequestEntity leaveRequest = leaveRequestOptional.get();
            return leaveRequest.getEmployee().getUser().getUsername().equals(userDetails.getUsername());
        }
        
        return false;
    }
    
    public boolean isNotificationOwner(Authentication authentication, Long notificationId) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        Optional<NotificationEntity> notificationOptional = notificationRepository.findById(notificationId);
        
        if (notificationOptional.isPresent()) {
            NotificationEntity notification = notificationOptional.get();
            return notification.getUser().getUsername().equals(userDetails.getUsername());
        }
        
        return false;
    }
} 