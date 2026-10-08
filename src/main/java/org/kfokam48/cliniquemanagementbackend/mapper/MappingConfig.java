package org.kfokam48.cliniquemanagementbackend.mapper;

import org.kfokam48.cliniquemanagementbackend.dto.utilisateur.UtilisateurDTO;
import org.kfokam48.cliniquemanagementbackend.model.Utilisateur;
import org.mapstruct.Builder;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.MapperConfig;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingInheritanceStrategy;
import org.mapstruct.NullValueMappingStrategy;
import org.mapstruct.ReportingPolicy;

/**
 * Configuration commune des mappers MapStruct (implémentations générées à la compilation).
 * <ul>
 *   <li>beans Spring, injectables comme les anciens {@code @Component} ; les mappers utilisés
 *       ({@code uses}) sont reçus par le constructeur ;</li>
 *   <li>un champ cible ni alimenté ni explicitement ignoré fait échouer la compilation : un champ
 *       ajouté à un DTO ne peut plus rester silencieusement vide ;</li>
 *   <li>une liste source null donne une liste vide (et non null) ;</li>
 *   <li>pas de builder Lombok : les objets sont créés avec {@code new} puis les setters.</li>
 * </ul>
 */
@MapperConfig(
        componentModel = MappingConstants.ComponentModel.SPRING,
        injectionStrategy = InjectionStrategy.CONSTRUCTOR,
        unmappedTargetPolicy = ReportingPolicy.ERROR,
        nullValueIterableMappingStrategy = NullValueMappingStrategy.RETURN_DEFAULT,
        builder = @Builder(disableBuilder = true),
        mappingInheritanceStrategy = MappingInheritanceStrategy.AUTO_INHERIT_FROM_CONFIG)
public interface MappingConfig {

    /**
     * Modèle hérité par toute création de compte (médecin, secrétaire, administrateur) depuis sa
     * requête : ces champs sont gérés par l'application, jamais fournis par le client.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "derniereConnexion", ignore = true)
    @Mapping(target = "dateCreation", ignore = true)
    @Mapping(target = "adresse", ignore = true)
    @Mapping(target = "actif", ignore = true)
    Utilisateur nouveauCompte(UtilisateurDTO utilisateurDTO);
}
