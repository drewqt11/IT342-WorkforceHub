package cit.edu.workforce.Service;

import cit.edu.workforce.Entity.DepartmentEntity;
import cit.edu.workforce.Repository.DepartmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class DepartmentService {

    private final DepartmentRepository departmentRepository;

    @Autowired
    public DepartmentService(DepartmentRepository departmentRepository) {
        this.departmentRepository = departmentRepository;
    }

    @Transactional(readOnly = true)
    public List<DepartmentEntity> getAllDepartments() {
        return departmentRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<DepartmentEntity> getDepartmentById(String departmentId) {
        return departmentRepository.findById(departmentId);
    }

    @Transactional(readOnly = true)
    public Optional<DepartmentEntity> findById(String departmentId) {
        return departmentRepository.findById(departmentId);
    }

    @Transactional(readOnly = true)
    public Optional<DepartmentEntity> getDepartmentByName(String departmentName) {
        return departmentRepository.findByDepartmentName(departmentName);
    }

    @Transactional
    public DepartmentEntity createDepartment(String departmentName, String description) {
        DepartmentEntity department = new DepartmentEntity();
        department.setDepartmentName(departmentName);
        department.setDescription(description);
        return departmentRepository.save(department);
    }

    @Transactional
    public DepartmentEntity updateDepartment(String departmentId, String departmentName, String description) {
        DepartmentEntity department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new RuntimeException("Department not found"));

        department.setDepartmentName(departmentName);
        department.setDescription(description);
        return departmentRepository.save(department);
    }

    @Transactional
    public void deleteDepartment(String departmentId) {
        departmentRepository.deleteById(departmentId);
    }
}