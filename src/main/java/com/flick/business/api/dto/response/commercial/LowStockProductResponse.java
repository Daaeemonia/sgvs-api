package com.flick.business.api.dto.response.commercial;

import java.math.BigDecimal; 

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LowStockProductResponse {
    private Long id;
    private String name;
    private BigDecimal currentStock;
    private Integer minimumStock;
}
