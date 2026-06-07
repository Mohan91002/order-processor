package com.learning.orderprocessor.controller;

import com.learning.orderprocessor.domain.Order;
import com.learning.orderprocessor.domain.OrderStatus;
import com.learning.orderprocessor.dto.CreateOrderRequest;
import com.learning.orderprocessor.dto.OrderResponse;
import com.learning.orderprocessor.repo.OrderRepository;
import com.learning.orderprocessor.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

// Demonstrates: REST controller + @Valid + @PreAuthorize (method-level RBAC)
@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orders;
    private final OrderRepository repo;

    public OrderController(OrderService orders, OrderRepository repo) {
        this.orders = orders;
        this.repo = repo;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public ResponseEntity<OrderResponse> create(@Valid @RequestBody CreateOrderRequest req) {
        Order saved = orders.createOrder(req);
        return ResponseEntity.created(URI.create("/api/orders/" + saved.getId()))
                .body(OrderResponse.from(saved));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public OrderResponse get(@PathVariable Long id) {
        return OrderResponse.from(orders.getRequired(id));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public Page<OrderResponse> list(@RequestParam(required = false) OrderStatus status,
                                    @RequestParam(defaultValue = "0") int page,
                                    @RequestParam(defaultValue = "20") int size) {
        var pageReq = PageRequest.of(page, size);
        Page<Order> result = status == null ? repo.findAll(pageReq) : repo.findByStatus(status, pageReq);
        return result.map(OrderResponse::from);
    }

    @PatchMapping("/{id}/status/{status}")
    @PreAuthorize("hasRole('ADMIN')")
    public void updateStatus(@PathVariable Long id, @PathVariable OrderStatus status) {
        orders.markStatus(id, status);
    }
}
