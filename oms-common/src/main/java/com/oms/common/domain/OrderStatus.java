package com.oms.common.domain;

/**
 * Constrained order lifecycle (Req 6.1).
 * Created → Allocated → Delivered_To_Customer
 */
public enum OrderStatus {
    Created,
    Allocated,
    Delivered_To_Customer
}
