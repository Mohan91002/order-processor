package com.learning.orderprocessor.repo;

import com.learning.orderprocessor.domain.Order;
import com.learning.orderprocessor.domain.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {

    Page<Order> findByStatus(OrderStatus status, Pageable pageable);

    List<Order> findByCreatedAtAfter(Instant after);

    @Query("select count(o) from Order o where o.status = :status and o.createdAt > :after")
    long countByStatusSince(@Param("status") OrderStatus status, @Param("after") Instant after);

    @Query("select coalesce(sum(o.totalCents), 0) from Order o where o.createdAt > :after")
    long sumTotalSince(@Param("after") Instant after);
}
