package com.oms.notification.service;

public interface NotificationSender {
    void send(String customerId, String orderId);
}
