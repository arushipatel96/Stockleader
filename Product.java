package com.stockledger.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false, unique = true)
    private String sku;

    @NotBlank
    @Column(nullable = false)
    private String name;

    @NotBlank
    @Column(nullable = false)
    private String category;

    @NotBlank
    @Column(nullable = false)
    private String supplier;

    @Positive
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal unitPrice;

    @Min(0)
    @Column(nullable = false)
    private Integer stockQuantity;

    @Min(0)
    @Column(nullable = false)
    private Integer reorderLevel;

    @Column(nullable = false)
    private String warehouseBin;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public Product() {}

    public Product(String sku, String name, String category, String supplier,
                   BigDecimal unitPrice, Integer stockQuantity, Integer reorderLevel, String warehouseBin) {
        this.sku = sku;
        this.name = name;
        this.category = category;
        this.supplier = supplier;
        this.unitPrice = unitPrice;
        this.stockQuantity = stockQuantity;
        this.reorderLevel = reorderLevel;
        this.warehouseBin = warehouseBin;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getSupplier() { return supplier; }
    public void setSupplier(String supplier) { this.supplier = supplier; }

    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }

    public Integer getStockQuantity() { return stockQuantity; }
    public void setStockQuantity(Integer stockQuantity) { this.stockQuantity = stockQuantity; }

    public Integer getReorderLevel() { return reorderLevel; }
    public void setReorderLevel(Integer reorderLevel) { this.reorderLevel = reorderLevel; }

    public String getWarehouseBin() { return warehouseBin; }
    public void setWarehouseBin(String warehouseBin) { this.warehouseBin = warehouseBin; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
