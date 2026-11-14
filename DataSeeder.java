package com.stockledger.config;

import com.stockledger.model.Product;
import com.stockledger.repository.ProductRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.util.List;

@Configuration
public class DataSeeder {

    @Bean
    CommandLineRunner seedInventory(ProductRepository productRepository) {
        return args -> {
            if (productRepository.count() == 0) {
                productRepository.saveAll(List.of(
                        new Product("ELC-1042", "Industrial PoE+ Network Switch 24-Port", "Electronics", "DeltaGrid Systems", new BigDecimal("429.00"), 34, 10, "A1-04"),
                        new Product("ELC-1088", "Thermal Barcode Label Printer 300dpi", "Electronics", "PrintCore Hardware", new BigDecimal("215.50"), 8, 12, "A2-11"),
                        new Product("FUR-2019", "Ergonomic Warehouse Packing Workstation", "Furniture", "SteelCraft Ind.", new BigDecimal("680.00"), 14, 5, "B4-02"),
                        new Product("PKG-3005", "Heavy-Duty Corrugated Shipping Cartons (50pk)", "Packaging", "KraftPack Co.", new BigDecimal("64.00"), 120, 30, "C1-01"),
                        new Product("SAF-4012", "ANSI Z87.1 Laser Safety Goggles", "Safety", "OptiShield Labs", new BigDecimal("38.75"), 6, 15, "D3-08")
                ));
            }
        };
    }
}
