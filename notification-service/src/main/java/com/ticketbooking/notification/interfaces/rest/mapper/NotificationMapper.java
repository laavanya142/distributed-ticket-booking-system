package com.ticketbooking.notification.interfaces.rest.mapper;

import com.ticketbooking.notification.application.model.ProcessNotificationCommand;
import com.ticketbooking.notification.domain.entity.Notification;
import com.ticketbooking.notification.infrastructure.kafka.dto.NotificationEventDto;
import com.ticketbooking.notification.interfaces.rest.dto.NotificationResponse;
import java.util.List;
import org.mapstruct.Mapper;

/**
 * MapStruct mapper converting between notification domain entities, REST responses, and Kafka event DTOs.
 */
@Mapper(componentModel = "spring")
public interface NotificationMapper {

    /**
     * Maps Notification entity to NotificationResponse DTO.
     *
     * @param notification Notification entity.
     * @return NotificationResponse DTO.
     */
    NotificationResponse toNotificationResponse(Notification notification);

    /**
     * Maps list of Notification entities to list of NotificationResponse DTOs.
     *
     * @param notifications List of Notification entities.
     * @return List of NotificationResponse DTOs.
     */
    List<NotificationResponse> toNotificationResponseList(List<Notification> notifications);

    /**
     * Maps NotificationEventDto to ProcessNotificationCommand.
     *
     * @param eventDto Kafka event payload DTO.
     * @return ProcessNotificationCommand object.
     */
    ProcessNotificationCommand toProcessNotificationCommand(NotificationEventDto eventDto);
}
