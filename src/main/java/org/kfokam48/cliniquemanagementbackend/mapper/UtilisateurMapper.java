package org.kfokam48.cliniquemanagementbackend.mapper;

import org.kfokam48.cliniquemanagementbackend.dto.utilisateur.Contact;
import org.kfokam48.cliniquemanagementbackend.dto.utilisateur.UtilisateurDTO;
import org.kfokam48.cliniquemanagementbackend.dto.utilisateur.UtilisateurResponseDTO;
import org.kfokam48.cliniquemanagementbackend.dto.auth.UserDTO;
import org.kfokam48.cliniquemanagementbackend.model.Utilisateur;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class UtilisateurMapper {
    private final ModelMapper modelMapper;

    public UtilisateurMapper(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }
    public Utilisateur utilisateurDtoToUtilisateur (UtilisateurDTO utilisateurDTO){
        return modelMapper.map(utilisateurDTO, Utilisateur.class);
    }
    public UtilisateurDTO utilisateurToUtilisateurDto (Utilisateur utilisateur){
        return modelMapper.map(utilisateur, UtilisateurDTO.class);
    }
    public UtilisateurResponseDTO utilisateurToUtilisateurResponseDTO (Utilisateur utilisateur){
        return modelMapper.map(utilisateur, UtilisateurResponseDTO.class);
    }

    public Contact utilisateurToContact (Utilisateur user){
        Contact contact = new Contact();
        contact.setStatus(user.getStatus());
        contact.setId(user.getId());
        contact.setEmail(user.getEmail());
        contact.setNom(user.getNom());
        contact.setPrenom(user.getPrenom());
        contact.setDerniereConnexion(user.getDerniereConnexion());
        contact.setRole(user.getRole().name());
        return contact;


    }
    public List<Contact> utilisateursToContacts(List<Utilisateur> utilisateurs) {
        return utilisateurs.stream()
                .map(this::utilisateurToContact)
                .collect(Collectors.toList());
    }
    public List<UtilisateurResponseDTO> utilisateursToUtilisateurResponseDTOs (List<Utilisateur> utilisateurs){
        return utilisateurs.stream()
                .map(this::utilisateurToUtilisateurResponseDTO)
                .collect(Collectors.toList());
    }

    public UserDTO utilisateurToUserDTO(Utilisateur utilisateur){
        return modelMapper.map(utilisateur, UserDTO.class);
    }

}
