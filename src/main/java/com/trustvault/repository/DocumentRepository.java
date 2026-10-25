package com.trustvault.repository;
import com.trustvault.model.DocumentRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface DocumentRepository extends JpaRepository<DocumentRecord,Long> {
    List<DocumentRecord> findByOwnerUsername(String ownerUsername);
}
