package org.kfokam48.cliniquemanagementbackend.mapper;

import org.kfokam48.cliniquemanagementbackend.dto.notification.NotificationResponseDTO;
import org.kfokam48.cliniquemanagementbackend.model.Notification;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(config = MappingConfig.class)
public interface NotificationMapper {

    NotificationResponseDTO notficationToNotificationResponseDTO(Notification notification);

    List<NotificationResponseDTO> notificationListToNotificationResponseDTOList(List<Notification> notificationList);
}
