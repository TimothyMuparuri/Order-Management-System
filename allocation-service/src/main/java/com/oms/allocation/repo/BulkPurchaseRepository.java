package com.oms.allocation.repo;

import com.oms.allocation.domain.BulkPurchase;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BulkPurchaseRepository extends JpaRepository<BulkPurchase, String> {
}
