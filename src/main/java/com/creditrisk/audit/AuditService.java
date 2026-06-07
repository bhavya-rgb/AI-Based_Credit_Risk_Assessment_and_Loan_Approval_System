package com.creditrisk.audit;

import com.creditrisk.common.JsonSupport;
import com.creditrisk.user.UserEntity;
import com.creditrisk.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuditService {
    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;
    private final JsonSupport jsonSupport;

    public AuditService(AuditLogRepository auditLogRepository, UserRepository userRepository, JsonSupport jsonSupport) {
        this.auditLogRepository = auditLogRepository;
        this.userRepository = userRepository;
        this.jsonSupport = jsonSupport;
    }

    @Transactional
    public void log(Long actorUserId, String actionType, String entityType, String entityId, Object before, Object after, String ipAddress) {
        AuditLogEntity log = new AuditLogEntity();
        if (actorUserId != null) {
            userRepository.findById(actorUserId).ifPresent(log::setActorUser);
        }
        log.setActionType(actionType);
        log.setEntityType(entityType);
        log.setEntityId(entityId);
        log.setBeforeJson(before == null ? null : jsonSupport.toJson(before));
        log.setAfterJson(after == null ? null : jsonSupport.toJson(after));
        log.setIpAddress(ipAddress);
        auditLogRepository.save(log);
    }
}
