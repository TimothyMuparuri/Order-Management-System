package com.oms.allocation.repo;

import com.oms.allocation.domain.Allocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface AllocationRepository extends JpaRepository<Allocation, String> {
    @Query("select coalesce(sum(a.quantity),0) from Allocation a where a.bulkLineId = :bulkLineId")
    int totalForBulkLine(String bulkLineId);

    @Query("select coalesce(sum(a.quantity),0) from Allocation a where a.orderLineId = :orderLineId")
    int totalForOrderLine(String orderLineId);

    List<Allocation> findByOrderId(String orderId);
}
