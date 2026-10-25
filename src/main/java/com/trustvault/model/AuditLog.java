package com.trustvault.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
public class AuditLog {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable=false) private String actor;
    @Column(nullable=false) private String action;
    private Long documentId;
    @Column(nullable=false, length=1000) private String details;
    @Column(nullable=false) private Instant eventTime = Instant.now();
    public AuditLog() {}
    public AuditLog(String actor,String action,Long documentId,String details){this.actor=actor;this.action=action;this.documentId=documentId;this.details=details;}
    public Long getId(){return id;} public String getActor(){return actor;} public String getAction(){return action;}
    public Long getDocumentId(){return documentId;} public String getDetails(){return details;} public Instant getEventTime(){return eventTime;}
}
