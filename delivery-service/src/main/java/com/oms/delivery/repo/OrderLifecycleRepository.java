package com.oms.delivery.repo;

import com.oms.delivery.domain.OrderLifecycle;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderLifecycleRepository extends JpaRepository<OrderLifecycle, String> {
}
