package com.flick.business.service;

import com.flick.business.client.BCBClient;
import com.flick.business.core.entity.InflationRate;
import com.flick.business.exception.InflationDataNotFoundException;
import com.flick.business.repository.InflationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class InflationService {
    
    private final InflationRepository inflationRepository;
    private final BCBClient bcbClient;
    
    /**
     * Calcula inflação acumulada dos últimos N meses
     */
    public BigDecimal getAccumulatedInflation(int months, String indexType) {
        if (months <= 0) {
            return BigDecimal.ZERO;
        }
        
        LocalDate endDate = LocalDate.now().withDayOfMonth(1);
        LocalDate startDate = endDate.minusMonths(months);
        
        Optional<BigDecimal> averageRate = switch (indexType.toUpperCase()) {
            case "IPCA" -> inflationRepository.findAverageIpcaBetween(startDate, endDate);
            case "INPC" -> inflationRepository.findAverageInpcBetween(startDate, endDate);
            case "IGPM" -> inflationRepository.findAverageInpcBetween(startDate, endDate);
            default -> inflationRepository.findAverageIpcaBetween(startDate, endDate);
        };
        
        if (averageRate.isPresent()) {
            BigDecimal monthlyAvg = averageRate.get();
            // Fórmula: (1 + taxa_mensal)^meses - 1
            BigDecimal monthlyMultiplier = BigDecimal.ONE.add(
                monthlyAvg.divide(BigDecimal.valueOf(100), 6, RoundingMode.HALF_UP)
            );
            
            BigDecimal accumulated = monthlyMultiplier.pow(months);
            return accumulated.subtract(BigDecimal.ONE)
                            .multiply(BigDecimal.valueOf(100))
                            .setScale(2, RoundingMode.HALF_UP);
        }
        
        // Se não tem dados, busca da API
        return fetchAndCalculateAccumulatedInflation(months, indexType);
    }
    
    /**
     * Busca dados da API e calcula inflação
     */
    private BigDecimal fetchAndCalculateAccumulatedInflation(int months, String indexType) {
        try {
            log.info("Buscando dados de inflação da API para {} meses, índice {}", months, indexType);
            
            Map<String, BigDecimal> latestRates = bcbClient.getLatestInflationRates();
            BigDecimal monthlyRate = latestRates.getOrDefault(indexType, BigDecimal.ZERO);
            
            if (monthlyRate.compareTo(BigDecimal.ZERO) > 0) {
                // Salva para uso futuro
                saveInflationRate(monthlyRate, indexType);
                
                // Calcula acumulado
                BigDecimal monthlyMultiplier = BigDecimal.ONE.add(
                    monthlyRate.divide(BigDecimal.valueOf(100), 6, RoundingMode.HALF_UP)
                );
                
                BigDecimal accumulated = monthlyMultiplier.pow(months);
                return accumulated.subtract(BigDecimal.ONE)
                                .multiply(BigDecimal.valueOf(100))
                                .setScale(2, RoundingMode.HALF_UP);
            }
            
        } catch (Exception e) {
            log.error("Erro ao buscar inflação da API: {}", e.getMessage());
        }
        
        // Retorna taxa padrão se não conseguir buscar
        return getDefaultInflationRate(months);
    }
    
    /**
     * Taxa padrão de inflação (usada como fallback)
     */
    private BigDecimal getDefaultInflationRate(int months) {
        // Taxas médias históricas do Brasil
        BigDecimal annualRate;
        if (months <= 3) {
            annualRate = new BigDecimal("0.35");  // 0,35% ao mês ≈ 4,2% ao ano
        } else if (months <= 6) {
            annualRate = new BigDecimal("0.40");  // 0,40% ao mês ≈ 4,8% ao ano
        } else {
            annualRate = new BigDecimal("0.45");  // 0,45% ao mês ≈ 5,4% ao ano
        }
        
        BigDecimal monthlyMultiplier = BigDecimal.ONE.add(
            annualRate.divide(BigDecimal.valueOf(100), 6, RoundingMode.HALF_UP)
        );
        
        BigDecimal accumulated = monthlyMultiplier.pow(months);
        return accumulated.subtract(BigDecimal.ONE)
                        .multiply(BigDecimal.valueOf(100))
                        .setScale(2, RoundingMode.HALF_UP);
    }
    
    /**
     * Calcula preço com inflação
     */
    public BigDecimal calculatePriceWithInflation(BigDecimal basePrice,
                                                  BigDecimal profitMarginPercentage,
                                                  Integer inflationMonths,
                                                  String indexType) {
        if (basePrice == null || basePrice.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Preço base inválido");
        }
        
        // 1. Aplica margem de lucro
        BigDecimal priceWithMargin = basePrice;
        if (profitMarginPercentage != null && profitMarginPercentage.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal marginMultiplier = BigDecimal.ONE.add(
                profitMarginPercentage.divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP)
            );
            priceWithMargin = basePrice.multiply(marginMultiplier);
        }
        
        // 2. Aplica inflação se solicitado
        if (inflationMonths != null && inflationMonths > 0) {
            BigDecimal inflationRate = getAccumulatedInflation(inflationMonths, 
                indexType != null ? indexType : "IPCA");
            
            BigDecimal inflationMultiplier = BigDecimal.ONE.add(
                inflationRate.divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP)
            );
            
            priceWithMargin = priceWithMargin.multiply(inflationMultiplier);
        }
        
        return priceWithMargin.setScale(2, RoundingMode.HALF_UP);
    }
    
    /**
     * Salva taxa de inflação no banco
     */
    @Transactional
    public void saveInflationRate(BigDecimal rate, String indexType) {
        LocalDate monthYear = LocalDate.now().withDayOfMonth(1);
        
        InflationRate inflationRate = inflationRepository.findByMonthYear(monthYear)
                .orElse(InflationRate.builder()
                        .monthYear(monthYear)
                        .build());
        
        switch (indexType.toUpperCase()) {
            case "IPCA" -> inflationRate.setIpcaRate(rate);
            case "INPC" -> inflationRate.setInpcRate(rate);
            case "IGPM" -> inflationRate.setIgpmRate(rate);
            case "IPCA15" -> inflationRate.setIpca15Rate(rate);
        }
        
        inflationRate.setSource("BCB");
        inflationRepository.save(inflationRate);
        
        log.info("Taxa de inflação {} salva: {}% para {}", indexType, rate, monthYear);
    }
    
    /**
     * Obtém histórico de inflação
     */
    public List<InflationRate> getInflationHistory(int limit) {
        LocalDate today = LocalDate.now().withDayOfMonth(1);
        return inflationRepository.findLastMonths(today, limit);
    }
}
