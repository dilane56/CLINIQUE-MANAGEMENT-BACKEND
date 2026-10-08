package org.kfokam48.cliniquemanagementbackend.service;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/** Compteurs pour les tableaux de bord, calculés en base par une requête "SELECT valeur, COUNT(*) ... GROUP BY valeur". */
public final class Compteurs {

    private Compteurs() {
    }

    /**
     * @param lignes résultat de la requête : [valeur de l'enum, nombre]
     * @return un compteur pour chaque valeur de l'enum (0 si aucune ligne), dans l'ordre de l'enum
     */
    @SuppressWarnings("unchecked")
    public static <E extends Enum<E>> Map<E, Long> parValeur(Class<E> type, List<Object[]> lignes) {
        Map<E, Long> compteurs = new EnumMap<>(type);
        for (E valeur : type.getEnumConstants()) {
            compteurs.put(valeur, 0L);
        }
        for (Object[] ligne : lignes) {
            if (ligne[0] != null) {
                compteurs.put((E) ligne[0], (Long) ligne[1]);
            }
        }
        return compteurs;
    }
}
