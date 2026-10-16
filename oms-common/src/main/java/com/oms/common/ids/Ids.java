package com.oms.common.ids;

public final class Ids {
    private Ids() {}

    public record CustomerId(String value) {
        public CustomerId {
            require(value, "customerId");
        }
        @Override public String toString() { return value; }
    }

    public record CustomerOrderId(String value) {
        public CustomerOrderId {
            require(value, "orderId");
        }
        @Override public String toString() { return value; }
    }

    public record OrderLineId(String value) {
        public OrderLineId {
            require(value, "orderLineId");
        }
        @Override public String toString() { return value; }
    }

    public record BulkPurchaseId(String value) {
        public BulkPurchaseId {
            require(value, "bulkPurchaseId");
        }
        @Override public String toString() { return value; }
    }

    public record BulkPurchaseLineId(String value) {
        public BulkPurchaseLineId {
            require(value, "bulkPurchaseLineId");
        }
        @Override public String toString() { return value; }
    }

    public record AllocationId(String value) {
        public AllocationId {
            require(value, "allocationId");
        }
        @Override public String toString() { return value; }
    }

    public record ImageId(String value) {
        public ImageId {
            require(value, "imageId");
        }
        @Override public String toString() { return value; }
    }

    public record ItemRef(String value) {
        public ItemRef {
            require(value, "itemRef");
        }
        @Override public String toString() { return value; }
    }

    private static void require(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " required");
        }
    }
}
