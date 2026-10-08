package org.kfokam48.cliniquemanagementbackend.mapper;

import org.kfokam48.cliniquemanagementbackend.dto.AdministrateurDTO;
import org.kfokam48.cliniquemanagementbackend.model.Administrateur;
import org.mapstruct.Mapper;

@Mapper(config = MappingConfig.class)
public interface AdministrateurMapper {

    Administrateur administrateurDtoToAdministrateur(AdministrateurDTO administrateurDTO);
}
