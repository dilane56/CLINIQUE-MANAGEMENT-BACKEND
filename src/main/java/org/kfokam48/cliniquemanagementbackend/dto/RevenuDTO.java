package org.kfokam48.cliniquemanagementbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RevenuDTO {
    private BigDecimal revenuMensuel;
    private BigDecimal revenuMoisPrecedent;
    private Double pourcentageEvolution;
}