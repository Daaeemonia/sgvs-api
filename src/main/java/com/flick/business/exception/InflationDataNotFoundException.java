package com.flick.business.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class InflationDataNotFoundException extends RuntimeException {
    
    public InflationDataNotFoundException(String message) {
        super(message);
    }
    
    public InflationDataNotFoundException(String indexType, int months) {
        super(String.format("Dados de inflação não encontrados para índice %s nos últimos %d meses", 
                          indexType, months));
    }
}
