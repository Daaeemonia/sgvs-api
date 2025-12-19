package com.flick.business.api.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class BCBResponse {
    
    @JsonProperty("@odata.context")
    private String context;
    
    private List<BCBInflationValue> value;
    
    @Data
    public static class BCBInflationValue {
        @JsonProperty("SERCODIGO")
        private String seriesCode;
        
        @JsonProperty("DATA")
        private String date;
        
        @JsonProperty("VALOR")
        private String value;
        
        @JsonProperty("NOMESERIE")
        private String seriesName;
    }
}
