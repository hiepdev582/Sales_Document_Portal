package com.salesportal.repository;

import com.salesportal.entity.Department;
import com.salesportal.entity.DocumentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DocumentRepository extends JpaRepository<DocumentEntity, Long> {
    List<DocumentEntity> findByOwnerId(Long ownerId);
    List<DocumentEntity> findByDepartment(Department department);
}
