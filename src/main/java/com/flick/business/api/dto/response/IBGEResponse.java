package com.flick.business.api.dto.external.ibge;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class IBGEResponse {
    
    private List<IBGEInflationData> dados;
    
    @Data
    public static class IBGEInflationData {
        @JsonProperty("V")
        private String valor;
        
        @JsonProperty("D1C")
        private String codigoIndice;
        
        @JsonProperty("D1N")
        private String nomeIndice;
        
        @JsonProperty("D2C")
        private String codigoPeriodo;
        
        @JsonProperty("D2N")
        private String periodo;
        
        @JsonProperty("D3C")
        private String codigoVariavel;
        
        @JsonProperty("D3N")
        private String variavel;
    }
}
