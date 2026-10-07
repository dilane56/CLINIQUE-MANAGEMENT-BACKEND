package org.kfokam48.cliniquemanagementbackend.dto;

import org.springframework.data.domain.Page;

import java.util.List;
import java.util.function.Function;

/**
 * Réponse paginée des endpoints de liste (appelés avec ?page=...&size=...).
 * Format stable, indépendant de la sérialisation interne de Spring Data (PageImpl).
 */
public record PageResponse<T>(List<T> content, int page, int size, long totalElements, int totalPages) {

    // Convertit une page d'entités en réutilisant un mapper de liste existant
    public static <E, T> PageResponse<T> of(Page<E> page, Function<List<E>, List<T>> mapper) {
        return new PageResponse<>(mapper.apply(page.getContent()), page.getNumber(), page.getSize(),
                page.getTotalElements(), page.getTotalPages());
    }
}
