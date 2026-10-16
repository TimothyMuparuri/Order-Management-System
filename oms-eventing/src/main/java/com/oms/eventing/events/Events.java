package com.oms.eventing.events;

import java.util.List;

public final class Events {
    private Events() {}

    public record OrderLineRef(String orderLineId, String itemRef, int requestedQty) {}

    public record OrderCreated(
            String orderId,
            String customerId,
            List<OrderLineRef> lines,
            long createdAtEpochMillis
    ) {}

    public record OrderFullyAllocated(
            String orderId,
            long allocatedAtEpochMillis
    ) {}

    public record BulkPurchaseReceived(
            String bulkPurchaseId,
            String bulkLineId,
            long receivedQty
    ) {}

    public record OrderDeliveredToCustomer(
            String orderId,
            String customerId,
            long deliveredAtEpochMillis,
            long adminToCustomerDurationSeconds
    ) {}

    public record NotificationRequested(
            String orderId,
            String customerId,
            long deliveredAtEpochMillis
    ) {}

    public record ImageUploaded(
            String imageId,
            String objectKey,
            String contentType,
            long sizeBytes
    ) {}

    public record Candidate(String itemRef, double confidence) {}

    public record ImageMatchSuggested(
            String imageId,
            List<Candidate> candidates
    ) {}
}
