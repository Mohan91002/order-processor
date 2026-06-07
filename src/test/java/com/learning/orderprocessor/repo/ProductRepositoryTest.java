package com.learning.orderprocessor.repo;

import com.learning.orderprocessor.domain.Product;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

// Demonstrates: @DataJpaTest slice — only JPA beans, no web/security/Kafka
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class ProductRepositoryTest {

    @Autowired
    private ProductRepository products;

    @Test
    void seededProductsArePresent() {
        assertThat(products.count()).isGreaterThanOrEqualTo(5);
        assertThat(products.findBySku("SKU-001")).isPresent();
    }

    @Test
    void decrementStockReducesStockAtomically() {
        Product p = products.findBySku("SKU-001").orElseThrow();
        int before = p.getStock();
        int updated = products.decrementStock(p.getId(), 3);
        assertThat(updated).isEqualTo(1);
        Product after = products.findById(p.getId()).orElseThrow();
        assertThat(after.getStock()).isEqualTo(before - 3);
    }
}
