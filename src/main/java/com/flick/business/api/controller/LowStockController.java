package com.flick.business.api.controller;

import com.flick.business.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/low-stock")
@RequiredArgsConstructor
public class LowStockController {
    
    private final ProductService productService;
    
    @GetMapping
    public String getLowStock() {
        return "Low stock endpoint - will implement later";
    }
}
