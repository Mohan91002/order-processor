package com.learning.orderprocessor.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "order_items")
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(nullable = false)
    private int quantity;

    @Column(name = "unit_price_cents", nullable = false)
    private long unitPriceCents;

    public OrderItem() {}

    public static Builder builder() { return new Builder(); }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Order getOrder() { return order; }
    public void setOrder(Order order) { this.order = order; }
    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public long getUnitPriceCents() { return unitPriceCents; }
    public void setUnitPriceCents(long unitPriceCents) { this.unitPriceCents = unitPriceCents; }

    public static final class Builder {
        private Long productId;
        private int quantity;
        private long unitPriceCents;

        public Builder productId(Long v) { this.productId = v; return this; }
        public Builder quantity(int v) { this.quantity = v; return this; }
        public Builder unitPriceCents(long v) { this.unitPriceCents = v; return this; }

        public OrderItem build() {
            OrderItem i = new OrderItem();
            i.productId = productId;
            i.quantity = quantity;
            i.unitPriceCents = unitPriceCents;
            return i;
        }
    }
}
