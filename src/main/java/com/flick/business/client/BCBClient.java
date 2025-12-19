package com.flick.business.client;

import com.flick.business.api.dto.external.bcb.BCBResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class BCBClient {
    
    private final RestTemplate restTemplate;
    
    @Value("${api.bcb.base-url:https://api.bcb.gov.br/dados/serie/bcdata.sgs}")
    private String bcbBaseUrl;
    
    // Códigos das séries do BCB
    private static final Map<String, String> BCB_SERIES = new HashMap<>();
    
    static {
        BCB_SERIES.put("IPCA", "433");          // IPCA - Índice oficial
        BCB_SERIES.put("IPCA15", "7478");       // IPCA-15 - Prévia
        BCB_SERIES.put("INPC", "188");          // INPC
        BCB_SERIES.put("IGP-M", "189");         // IGP-M
    }
    
    public BCBClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }
    
    public BigDecimal getInflationRate(String seriesCode, LocalDate date) {
        try {
            String url = buildBcbUrl(seriesCode, date);
            log.info("Buscando inflação do BCB: {}", url);
            
            BCBResponse response = restTemplate.getForObject(url, BCBResponse.class);
            
            if (response != null && response.getValue() != null && !response.getValue().isEmpty()) {
                String valorStr = response.getValue().get(0).getValue();
                return new BigDecimal(valorStr.replace(",", "."));
            }
            
        } catch (Exception e) {
            log.error("Erro ao buscar inflação do BCB para série {}: {}", seriesCode, e.getMessage());
        }
        
        return null;
    }
    
    private String buildBcbUrl(String seriesCode, LocalDate date) {
        String seriesId = BCB_SERIES.getOrDefault(seriesCode, seriesCode);
        
        return UriComponentsBuilder.fromHttpUrl(bcbBaseUrl + "." + seriesId + "/dados")
                .queryParam("formato", "json")
                .queryParam("dataInicial", date.minusMonths(12).format(DateTimeFormatter.ofPattern("dd/MM/yyyy")))
                .queryParam("dataFinal", date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")))
                .build()
                .toUriString();
    }
    
    public Map<String, BigDecimal> getLatestInflationRates() {
        Map<String, BigDecimal> rates = new HashMap<>();
        LocalDate today = LocalDate.now();
        
        for (Map.Entry<String, String> entry : BCB_SERIES.entrySet()) {
            BigDecimal rate = getInflationRate(entry.getKey(), today);
            if (rate != null) {
                rates.put(entry.getKey(), rate);
            }
        }
        
        return rates;
    }
}
