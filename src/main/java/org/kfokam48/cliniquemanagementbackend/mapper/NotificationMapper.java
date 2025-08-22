package org.kfokam48.cliniquemanagementbackend.mapper;

import lombok.RequiredArgsConstructor;
import org.kfokam48.cliniquemanagementbackend.dto.notification.NotificationResponseDTO;
import org.kfokam48.cliniquemanagementbackend.model.Notification;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class NotificationMapper {
    private final ModelMapper modelMapper;

    public NotificationResponseDTO notficationToNotificationResponseDTO(Notification notification){
        return modelMapper.map(notification, NotificationResponseDTO.class);
    }

    public List<NotificationResponseDTO> notificationListToNotificationResponseDTOList(List<Notification> notificationList){
        return  notificationList.stream()
                .map(this::notficationToNotificationResponseDTO)
                .toList();
    }
}
