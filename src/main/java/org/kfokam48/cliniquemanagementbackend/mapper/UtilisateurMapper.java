package org.kfokam48.cliniquemanagementbackend.mapper;

import org.kfokam48.cliniquemanagementbackend.dto.auth.UserDTO;
import org.kfokam48.cliniquemanagementbackend.dto.utilisateur.Contact;
import org.kfokam48.cliniquemanagementbackend.dto.utilisateur.UtilisateurResponseDTO;
import org.kfokam48.cliniquemanagementbackend.model.Utilisateur;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(config = MappingConfig.class)
public interface UtilisateurMapper {

    UtilisateurResponseDTO utilisateurToUtilisateurResponseDTO(Utilisateur utilisateur);

    List<UtilisateurResponseDTO> utilisateursToUtilisateurResponseDTOs(List<Utilisateur> utilisateurs);

    Contact utilisateurToContact(Utilisateur utilisateur);

    List<Contact> utilisateursToContacts(List<Utilisateur> utilisateurs);

    // Le sexe n'est connu que pour les patients, pas pour les comptes du personnel
    @Mapping(target = "sexe", ignore = true)
    UserDTO utilisateurToUserDTO(Utilisateur utilisateur);
}
