package com.trustvault.repository;
import com.trustvault.model.SharePermission;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
public interface SharePermissionRepository extends JpaRepository<SharePermission,Long> {
    List<SharePermission> findByRecipientUsernameAndActiveTrue(String username);
    Optional<SharePermission> findByDocumentIdAndRecipientUsername(Long documentId,String recipientUsername);
    List<SharePermission> findByDocumentId(Long documentId);
}
