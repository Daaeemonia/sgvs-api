@Component
public class InflationUpdateJob {
    
    @Scheduled(cron = "0 0 10 5 * ?") // Dia 5 de cada mês às 10:00
    public void updateInflationRates() {
        // Busca dados do BCB
        InflationData latestData = bcbClient.getLatestInflation();
        
        inflationRepository.save(InflationRate.builder()
            .monthYear(LocalDate.now().withDayOfMonth(1))
            .ipcaRate(latestData.getIpca())
            .inpcRate(latestData.getInpc())
            .igpmRate(latestData.getIgpm())
            .source("BCB")
            .build());
    }
}
