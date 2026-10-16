package com.oms.notification.repo;

import com.oms.notification.domain.NotificationRecord;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRecordRepository extends JpaRepository<NotificationRecord, String> {
}
