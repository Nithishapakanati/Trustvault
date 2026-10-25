package com.trustvault.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"documentId", "recipientUsername"}))
public class SharePermission {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable=false) private Long documentId;
    @Column(nullable=false) private String recipientUsername;
    @Column(nullable=false) private String permission = "READ";
    @Column(nullable=false) private boolean active = true;
    @Column(nullable=false) private Instant sharedAt = Instant.now();
    public SharePermission() {}
    public SharePermission(Long documentId,String recipientUsername,String permission){this.documentId=documentId;this.recipientUsername=recipientUsername;this.permission=permission;}
    public Long getId(){return id;} public Long getDocumentId(){return documentId;} public String getRecipientUsername(){return recipientUsername;}
    public String getPermission(){return permission;} public boolean isActive(){return active;} public Instant getSharedAt(){return sharedAt;}
    public void setActive(boolean active){this.active=active;}
}
