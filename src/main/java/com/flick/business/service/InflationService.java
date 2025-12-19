@Service
public class InflationService {
    
    private final InflationRepository inflationRepository;
    private final BCBClient bcbClient; // Cliente para Banco Central
    
    public BigDecimal getAccumulatedInflation(int months) {
        // Busca do banco de dados ou API externa
        return inflationRepository.findAverageInflationLastMonths(months);
    }
    
    public BigDecimal calculatePriceWithInflation(BigDecimal costPrice, 
                                                  BigDecimal profitMargin,
                                                  int inflationMonths) {
        BigDecimal inflation = getAccumulatedInflation(inflationMonths);
        BigDecimal profitMultiplier = BigDecimal.ONE.add(profitMargin.divide(BigDecimal.valueOf(100)));
        BigDecimal inflationMultiplier = BigDecimal.ONE.add(inflation.divide(BigDecimal.valueOf(100)));
        
        return costPrice.multiply(profitMultiplier)
                       .multiply(inflationMultiplier)
                       .setScale(2, RoundingMode.HALF_UP);
    }
}
