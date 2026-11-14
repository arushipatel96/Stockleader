package com.stockledger.controller;

import com.stockledger.model.CustomerOrder;
import com.stockledger.model.Product;
import com.stockledger.repository.OrderRepository;
import com.stockledger.repository.ProductRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class InventoryController {

    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;

    public InventoryController(ProductRepository productRepository, OrderRepository orderRepository) {
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
    }

    @GetMapping("/health")
    public Map<String, Object> health() {
        return Map.of(
                "status", "UP",
                "service", "stockledger-spring-boot",
                "javaVersion", System.getProperty("java.version"),
                "timestamp", Instant.now().toString()
        );
    }

    @GetMapping("/summary")
    public Map<String, Object> summary() {
        List<Product> products = productRepository.findAll();
        List<CustomerOrder> orders = orderRepository.findAll();

        BigDecimal inventoryValue = products.stream()
                .map(p -> p.getUnitPrice().multiply(BigDecimal.valueOf(p.getStockQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long lowStockCount = products.stream()
                .filter(p -> p.getStockQuantity() <= p.getReorderLevel())
                .count();

        BigDecimal fulfilledRevenue = orders.stream()
                .filter(o -> o.getStatus() != CustomerOrder.Status.CANCELLED)
                .map(CustomerOrder::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long activeOrders = orders.stream()
                .filter(o -> o.getStatus() == CustomerOrder.Status.PENDING
                        || o.getStatus() == CustomerOrder.Status.PROCESSING
                        || o.getStatus() == CustomerOrder.Status.SHIPPED)
                .count();

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("totalSkus", products.size());
        result.put("inventoryValue", inventoryValue);
        result.put("lowStockCount", lowStockCount);
        result.put("activeOrders", activeOrders);
        result.put("fulfilledRevenue", fulfilledRevenue);
        return result;
    }

    @GetMapping("/products")
    public List<Product> listProducts(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String category
    ) {
        return productRepository.findAll().stream()
                .filter(p -> category == null || category.isBlank() || category.equalsIgnoreCase("ALL")
                        || p.getCategory().equalsIgnoreCase(category))
                .filter(p -> search == null || search.isBlank()
                        || p.getName().toLowerCase().contains(search.toLowerCase())
                        || p.getSku().toLowerCase().contains(search.toLowerCase())
                        || p.getSupplier().toLowerCase().contains(search.toLowerCase()))
                .toList();
    }

    @PostMapping("/products")
    public ResponseEntity<Product> createProduct(@Valid @RequestBody Product request) {
        productRepository.findBySkuIgnoreCase(request.getSku()).ifPresent(existing -> {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "SKU already exists: " + request.getSku());
        });
        Product saved = productRepository.save(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/products/{id}")
    public Product updateProduct(@PathVariable Long id, @Valid @RequestBody Product request) {
        Product existing = productRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));
        existing.setSku(request.getSku());
        existing.setName(request.getName());
        existing.setCategory(request.getCategory());
        existing.setSupplier(request.getSupplier());
        existing.setUnitPrice(request.getUnitPrice());
        existing.setStockQuantity(request.getStockQuantity());
        existing.setReorderLevel(request.getReorderLevel());
        existing.setWarehouseBin(request.getWarehouseBin());
        return productRepository.save(existing);
    }

    @DeleteMapping("/products/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        if (!productRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found");
        }
        productRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
