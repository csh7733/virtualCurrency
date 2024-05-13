package practice.virtualcurrency.repository.order;

import org.springframework.data.jpa.repository.JpaRepository;
import practice.virtualcurrency.domain.order.Order;

public interface OrderRepository extends JpaRepository<Order,Long> {
}
