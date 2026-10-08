package org.kfokam48.cliniquemanagementbackend.mapper;

import org.kfokam48.cliniquemanagementbackend.dto.message.MessageResponseDTO;
import org.kfokam48.cliniquemanagementbackend.model.Message;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = MappingConfig.class)
public interface MessageMapper {

    @Mapping(target = "expediteurId", source = "expediteur.id")
    @Mapping(target = "expediteurNom", source = "expediteur.nom")
    @Mapping(target = "expediteurPrenom", source = "expediteur.prenom")
    @Mapping(target = "destinataireId", source = "destinataire.id")
    @Mapping(target = "destinataireNom", source = "destinataire.nom")
    @Mapping(target = "destinatairePrenom", source = "destinataire.prenom")
    @Mapping(target = "content", source = "contenu")
    @Mapping(target = "status", source = "messageStatus")
    MessageResponseDTO messageToMessageResponseDTO(Message message);
}
