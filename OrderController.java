package com.stockledger.controller;

import com.stockledger.model.CustomerOrder;
import com.stockledger.model.OrderItem;
import com.stockledger.model.Product;
import com.stockledger.repository.OrderRepository;
import com.stockledger.repository.ProductRepository;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = "*")
public class OrderController {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;

    public OrderController(OrderRepository orderRepository, ProductRepository productRepository) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
    }

    public record OrderLineRequest(Long productId, @Min(1) Integer quantity) {}

    public record CreateOrderRequest(
            @NotBlank String customerName,
            @Email @NotBlank String customerEmail,
            @NotBlank String shippingCity,
            @NotEmpty List<OrderLineRequest> items
    ) {}

    @GetMapping
    public List<CustomerOrder> listOrders(@RequestParam(required = false) String status) {
        if (status != null && !status.isBlank() && !status.equalsIgnoreCase("ALL")) {
            CustomerOrder.Status parsed = CustomerOrder.Status.valueOf(status.toUpperCase());
            return orderRepository.findByStatusOrderByCreatedAtDesc(parsed);
        }
        return orderRepository.findAllByOrderByCreatedAtDesc();
    }

    @PostMapping
    @Transactional
    public ResponseEntity<CustomerOrder> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        CustomerOrder order = new CustomerOrder();
        order.setOrderNumber("ORD-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase());
        order.setCustomerName(request.customerName());
        order.setCustomerEmail(request.customerEmail());
        order.setShippingCity(request.shippingCity());
        order.setStatus(CustomerOrder.Status.PENDING);

        BigDecimal total = BigDecimal.ZERO;
        for (OrderLineRequest line : request.items()) {
            Product product = productRepository.findById(line.productId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found: " + line.productId()));

            if (product.getStockQuantity() < line.quantity()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Insufficient stock for " + product.getName() + " (available: " + product.getStockQuantity() + ")");
            }

            product.setStockQuantity(product.getStockQuantity() - line.quantity());
            productRepository.save(product);

            OrderItem item = new OrderItem();
            item.setOrder(order);
            item.setProductId(product.getId());
            item.setProductName(product.getName());
            item.setSku(product.getSku());
            item.setQuantity(line.quantity());
            item.setUnitPrice(product.getUnitPrice());
            order.getItems().add(item);

            total = total.add(product.getUnitPrice().multiply(BigDecimal.valueOf(line.quantity())));
        }

        order.setTotalAmount(total);
        CustomerOrder saved = orderRepository.save(order);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PatchMapping("/{id}/status")
    @Transactional
    public CustomerOrder updateStatus(@PathVariable Long id, @RequestBody Map<String, String> payload) {
        CustomerOrder order = orderRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));

        CustomerOrder.Status nextStatus = CustomerOrder.Status.valueOf(payload.get("status").toUpperCase());
        if (order.getStatus() != CustomerOrder.Status.CANCELLED && nextStatus == CustomerOrder.Status.CANCELLED) {
            for (OrderItem item : order.getItems()) {
                productRepository.findById(item.getProductId()).ifPresent(product -> {
                    product.setStockQuantity(product.getStockQuantity() + item.getQuantity());
                    productRepository.save(product);
                });
            }
        }
        order.setStatus(nextStatus);
        return orderRepository.save(order);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOrder(@PathVariable Long id) {
        if (!orderRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found");
        }
        orderRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
