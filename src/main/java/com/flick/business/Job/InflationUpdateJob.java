package com.flick.business.job;

import com.flick.business.service.InflationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class InflationUpdateJob {
    
    private final InflationService inflationService;
    private final com.flick.business.client.BCBClient bcbClient;
    
    /**
     * Executa no dia 10 de cada mês às 02:00 AM
     * (Após liberação dos dados oficiais)
     */
    @Scheduled(cron = "0 0 2 10 * ?")  // Segundo Minuto Hora DiaDoMês Mês DiaDaSemana
    public void updateMonthlyInflation() {
        log.info("Iniciando atualização de dados de inflação: {}", LocalDateTime.now());
        
        try {
            Map<String, BigDecimal> latestRates = bcbClient.getLatestInflationRates();
            
            for (Map.Entry<String, BigDecimal> entry : latestRates.entrySet()) {
                if (entry.getValue() != null) {
                    inflationService.saveInflationRate(entry.getValue(), entry.getKey());
                }
            }
            
            log.info("Atualização de inflação concluída com sucesso. Taxas: {}", latestRates);
            
        } catch (Exception e) {
            log.error("Erro ao atualizar dados de inflação: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Executa todo dia 1º às 06:00 AM para atualizar IPCA-15 (prévia)
     */
    @Scheduled(cron = "0 0 6 1 * ?")
    public void updateIpca15Preview() {
        log.info("Atualizando IPCA-15 (prévia): {}", LocalDateTime.now());
        
        try {
            // IPCA-15 é geralmente divulgado no final do mês anterior
            LocalDate previousMonth = LocalDate.now().minusMonths(1);
            BigDecimal ipca15Rate = bcbClient.getInflationRate("IPCA15", previousMonth);
            
            if (ipca15Rate != null) {
                inflationService.saveInflationRate(ipca15Rate, "IPCA15");
                log.info("IPCA-15 atualizado: {}%", ipca15Rate);
            }
            
        } catch (Exception e) {
            log.error("Erro ao atualizar IPCA-15: {}", e.getMessage());
        }
    }
    
    /**
     * Verifica dados faltantes no dia 15 de cada mês
     */
    @Scheduled(cron = "0 0 12 15 * ?")
    public void checkMissingData() {
        log.info("Verificando dados de inflação faltantes...");
        
        LocalDate currentMonth = LocalDate.now().withDayOfMonth(1);
        LocalDate threeMonthsAgo = currentMonth.minusMonths(3);
        
        // Implementar lógica para verificar meses sem dados
        // e tentar buscar novamente da API
        
        log.info("Verificação de dados concluída");
    }
}
