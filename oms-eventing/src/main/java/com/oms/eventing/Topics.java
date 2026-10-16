package com.oms.eventing;

public final class Topics {
    private Topics() {}

    public static final String ORDERS_CREATED = "oms.orders.created";
    public static final String ORDERS_FULLY_ALLOCATED = "oms.orders.fully-allocated";
    public static final String BULK_RECEIVED = "oms.bulk.received";
    public static final String ORDERS_DELIVERED = "oms.orders.delivered";
    public static final String NOTIFICATIONS_REQUESTED = "oms.notifications.requested";
    public static final String IMAGES_UPLOADED = "oms.images.uploaded";
    public static final String IMAGES_MATCH_SUGGESTED = "oms.images.match-suggested";
}
