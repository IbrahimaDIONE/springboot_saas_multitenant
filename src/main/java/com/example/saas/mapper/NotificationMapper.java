package com.example.saas.mapper;

import com.example.saas.domain.Notification;
import com.example.saas.dto.NotificationResponse;
import org.springframework.stereotype.Component;

@Component
public class NotificationMapper {

    public NotificationResponse toResponse(Notification n) {
        return new NotificationResponse(
                n.getId(),
                n.getEtudiant().getId(),
                n.getMessage(),
                n.getType().name(),
                n.isLu(),
                n.getCreatedAt()
        );
    }
}