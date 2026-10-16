package com.oms.delivery.repo;

import com.oms.delivery.domain.StatusTransitionRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StatusTransitionRecordRepository extends JpaRepository<StatusTransitionRecord, String> {
    List<StatusTransitionRecord> findByOrderIdOrderByTimestampAsc(String orderId);
}
