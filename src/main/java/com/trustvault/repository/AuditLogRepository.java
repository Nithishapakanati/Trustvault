package com.trustvault.repository;
import com.trustvault.model.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface AuditLogRepository extends JpaRepository<AuditLog,Long> {
    List<AuditLog> findAllByOrderByEventTimeDesc();
}
