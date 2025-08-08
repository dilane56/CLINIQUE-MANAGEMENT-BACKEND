package org.kfokam48.cliniquemanagementbackend.mapper;

import org.kfokam48.cliniquemanagementbackend.dto.typeRendezVous.TypeRendezVousDTO;
import org.kfokam48.cliniquemanagementbackend.dto.typeRendezVous.TypeRendezVousResponseDTO;
import org.kfokam48.cliniquemanagementbackend.model.TypeRendezVous;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TypeRencezVousMapper {

    private final ModelMapper modelMapper;

    public TypeRencezVousMapper(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

    public TypeRendezVous typeRendezVousDtoToTypeRendezVous(TypeRendezVousDTO typeRendezVousDTO) {
      return modelMapper.map(typeRendezVousDTO, TypeRendezVous.class);
    }
    public TypeRendezVousResponseDTO typeRendezVousToTypeRendezVousResponseDTO(TypeRendezVous typeRendezVous) {
        return modelMapper.map(typeRendezVous, TypeRendezVousResponseDTO.class);
    }
    public List<TypeRendezVousResponseDTO> typeRendezVousListToTypeRendezVousResponseDTOList(List<TypeRendezVous> typeRendezVousList) {
        return typeRendezVousList.stream()
                .map(this::typeRendezVousToTypeRendezVousResponseDTO)
                .toList();
    }
}
