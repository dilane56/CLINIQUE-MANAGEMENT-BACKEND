package org.kfokam48.cliniquemanagementbackend.mapper;

import org.kfokam48.cliniquemanagementbackend.dto.typeRendezVous.TypeRendezVousDTO;
import org.kfokam48.cliniquemanagementbackend.dto.typeRendezVous.TypeRendezVousResponseDTO;
import org.kfokam48.cliniquemanagementbackend.model.TypeRendezVous;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(config = MappingConfig.class)
public interface TypeRendezVousMapper {

    @Mapping(target = "id", ignore = true)
    TypeRendezVous typeRendezVousDtoToTypeRendezVous(TypeRendezVousDTO typeRendezVousDTO);

    TypeRendezVousResponseDTO typeRendezVousToTypeRendezVousResponseDTO(TypeRendezVous typeRendezVous);

    List<TypeRendezVousResponseDTO> typeRendezVousListToTypeRendezVousResponseDTOList(List<TypeRendezVous> typeRendezVousList);
}
