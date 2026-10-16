package com.oms.allocation.repo;

import com.oms.allocation.domain.BulkPurchaseLine;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BulkPurchaseLineRepository extends JpaRepository<BulkPurchaseLine, String> {
    List<BulkPurchaseLine> findByBulkId(String bulkId);
}
