package com.practice.virtualcurrency.repository.order;

import com.practice.virtualcurrency.domain.order.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order,Long> {
}
