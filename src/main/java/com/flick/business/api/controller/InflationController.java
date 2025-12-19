package com.flick.business.api.controller;

import com.flick.business.core.entity.InflationRate;
import com.flick.business.service.InflationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/inflation")
@RequiredArgsConstructor
@Tag(name = "Inflation", description = "API para cálculo e consulta de inflação")
public class InflationController {
    
    private final InflationService inflationService;
    
    @GetMapping("/accumulated")
    @Operation(summary = "Calcula inflação acumulada")
    public ResponseEntity<BigDecimal> getAccumulatedInflation(
            @RequestParam(defaultValue = "12") int months,
            @RequestParam(defaultValue = "IPCA") String index) {
        
        BigDecimal inflation = inflationService.getAccumulatedInflation(months, index);
        return ResponseEntity.ok(inflation);
    }
    
    @GetMapping("/calculate-price")
    @Operation(summary = "Calcula preço com inflação")
    public ResponseEntity<BigDecimal> calculatePriceWithInflation(
            @RequestParam BigDecimal basePrice,
            @RequestParam BigDecimal profitMargin,
            @RequestParam(required = false) Integer inflationMonths,
            @RequestParam(defaultValue = "IPCA") String index) {
        
        BigDecimal finalPrice = inflationService.calculatePriceWithInflation(
            basePrice, profitMargin, inflationMonths, index);
        
        return ResponseEntity.ok(finalPrice);
    }
    
    @GetMapping("/history")
    @Operation(summary = "Obtém histórico de inflação")
    public ResponseEntity<List<InflationRate>> getInflationHistory(
            @RequestParam(defaultValue = "12") int months) {
        
        List<InflationRate> history = inflationService.getInflationHistory(months);
        return ResponseEntity.ok(history);
    }
}
