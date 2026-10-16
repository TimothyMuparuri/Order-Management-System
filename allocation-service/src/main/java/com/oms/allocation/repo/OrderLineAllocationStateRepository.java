package com.oms.allocation.repo;

import com.oms.allocation.domain.OrderLineAllocationState;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderLineAllocationStateRepository extends JpaRepository<OrderLineAllocationState, String> {
    List<OrderLineAllocationState> findByOrderId(String orderId);
}
