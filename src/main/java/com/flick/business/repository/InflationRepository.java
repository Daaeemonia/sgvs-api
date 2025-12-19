package com.flick.business.repository;

import com.flick.business.core.entity.InflationRate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface InflationRepository extends JpaRepository<InflationRate, Long> {
    
    Optional<InflationRate> findByMonthYear(LocalDate monthYear);
    
    List<InflationRate> findByMonthYearBetweenOrderByMonthYearDesc(
            LocalDate startDate, LocalDate endDate);
    
    @Query("SELECT ir FROM InflationRate ir WHERE ir.monthYear <= :date ORDER BY ir.monthYear DESC")
    List<InflationRate> findLatestUntilDate(@Param("date") LocalDate date, 
                                           org.springframework.data.domain.Pageable pageable);
    
    @Query("SELECT AVG(ir.ipcaRate) FROM InflationRate ir " +
           "WHERE ir.monthYear BETWEEN :startDate AND :endDate " +
           "AND ir.ipcaRate IS NOT NULL")
    Optional<BigDecimal> findAverageIpcaBetween(@Param("startDate") LocalDate startDate,
                                               @Param("endDate") LocalDate endDate);
    
    @Query("SELECT AVG(ir.inpcRate) FROM InflationRate ir " +
           "WHERE ir.monthYear BETWEEN :startDate AND :endDate " +
           "AND ir.inpcRate IS NOT NULL")
    Optional<BigDecimal> findAverageInpcBetween(@Param("startDate") LocalDate startDate,
                                               @Param("endDate") LocalDate endDate);
    
    @Query(value = "SELECT * FROM inflation_rates " +
           "WHERE month_year <= :date " +
           "ORDER BY month_year DESC LIMIT :months", nativeQuery = true)
    List<InflationRate> findLastMonths(@Param("date") LocalDate date, 
                                      @Param("months") int months);
}
