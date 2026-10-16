package com.oms.order.repo;

import com.oms.order.domain.CustomerOrderLine;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CustomerOrderLineRepository extends JpaRepository<CustomerOrderLine, String> {
    List<CustomerOrderLine> findByOrderId(String orderId);
}
