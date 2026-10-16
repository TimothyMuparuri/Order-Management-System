# Cross-service event wiring (task 13.1)

| Event | Topic | Producer (outbox) | Consumer | Effect |
|-------|-------|-------------------|----------|--------|
| OrderCreated | `oms.orders.created` | order-service | delivery-service | Status `Created` + first transition |
| OrderCreated | `oms.orders.created` | order-service | allocation-service | Register order-line allocation ceilings |
| OrderFullyAllocated | `oms.orders.fully-allocated` | allocation-service | delivery-service | `Created` → `Allocated` |
| BulkPurchaseReceived | `oms.bulk.received` | allocation-service | (future analytics) | Record received qty |
| OrderDeliveredToCustomer | `oms.orders.delivered` | delivery-service | notification-service (observes via NotificationRequested) | Delivery audit |
| NotificationRequested | `oms.notifications.requested` | delivery-service | notification-service | Send with retry ≤ 3 |
| ImageUploaded | `oms.images.uploaded` | intake-service | image-analysis-service | Vision match |
| ImageMatchSuggested | `oms.images.match-suggested` | image-analysis-service | intake / admin-bff (poll/UI) | Candidate confirmation |

All producers write an outbox row in the same DB transaction as the domain change. Relays publish keyed by aggregate id. Consumers dedupe via `processed_event`.
