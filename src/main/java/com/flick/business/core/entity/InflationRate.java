package com.flick.business.core.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZonedDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;


@Entity
@Table(name = "inflation_rates")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InflationRate {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "month_year", nullable = false)
    private LocalDate monthYear;  // Ex: 2024-01-01 (sempre dia 1)
    
    @Column(name = "ipca_rate", precision = 5, scale = 2)
    private BigDecimal ipcaRate;  // IPCA - Índice oficial
    
    @Column(name = "inpc_rate", precision = 5, scale = 2)
    private BigDecimal inpcRate;  // INPC - Para produtos de consumo
    
    @Column(name = "igpm_rate", precision = 5, scale = 2)
    private BigDecimal igpmRate;  // IGP-M - Para serviços/aluguel
    
    @Column(name = "ipca15_rate", precision = 5, scale = 2)
    private BigDecimal ipca15Rate;  // IPCA-15 - Prévia
    
    @Column(length = 50)
    private String source;  // BCB, IBGE, FGV, etc
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private ZonedDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private ZonedDateTime updatedAt;
}
