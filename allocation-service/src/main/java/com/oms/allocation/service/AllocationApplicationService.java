package com.oms.allocation.service;

import com.oms.allocation.domain.Allocation;
import com.oms.allocation.domain.BulkPurchase;
import com.oms.allocation.domain.BulkPurchaseLine;
import com.oms.allocation.domain.OrderLineAllocationState;
import com.oms.allocation.repo.AllocationRepository;
import com.oms.allocation.repo.BulkPurchaseLineRepository;
import com.oms.allocation.repo.BulkPurchaseRepository;
import com.oms.allocation.repo.OrderLineAllocationStateRepository;
import com.oms.common.result.Result;
import com.oms.eventing.Topics;
import com.oms.eventing.events.Events;
import com.oms.eventing.outbox.OutboxWriter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class AllocationApplicationService {

    private final BulkPurchaseRepository bulkPurchaseRepository;
    private final BulkPurchaseLineRepository bulkLineRepository;
    private final AllocationRepository allocationRepository;
    private final OrderLineAllocationStateRepository orderLineStateRepository;
    private final OutboxWriter outboxWriter;

    public AllocationApplicationService(
            BulkPurchaseRepository bulkPurchaseRepository,
            BulkPurchaseLineRepository bulkLineRepository,
            AllocationRepository allocationRepository,
            OrderLineAllocationStateRepository orderLineStateRepository,
            OutboxWriter outboxWriter
    ) {
        this.bulkPurchaseRepository = bulkPurchaseRepository;
        this.bulkLineRepository = bulkLineRepository;
        this.allocationRepository = allocationRepository;
        this.orderLineStateRepository = orderLineStateRepository;
        this.outboxWriter = outboxWriter;
    }

    @Transactional
    public Result<BulkPurchaseResponse> recordBulkPurchase(BulkPurchaseRequest request) {
        if (request.platformName() == null || request.platformName().isBlank() || request.platformName().length() > 100) {
            return Result.validation("INVALID_PLATFORM", "Platform name must be 1..100 characters");
        }
        if (request.placementDate() == null || request.placementDate().isAfter(LocalDate.now())) {
            return Result.validation("INVALID_PLACEMENT_DATE", "Placement date must be on or before today");
        }
        if (request.lines() == null || request.lines().isEmpty() || request.lines().size() > 500) {
            return Result.validation("INVALID_BULK_LINES", "Bulk purchase must have 1..500 lines");
        }
        for (BulkLineInput line : request.lines()) {
            if (line.purchasedQty() < 1 || line.purchasedQty() > 1_000_000) {
                return Result.validation("INVALID_PURCHASED_QTY", "purchasedQty must be 1..1000000");
            }
            if (line.unitCost() == null
                    || line.unitCost().compareTo(new BigDecimal("0.01")) < 0
                    || line.unitCost().compareTo(new BigDecimal("999999999.99")) > 0) {
                return Result.validation("INVALID_UNIT_COST", "unitCost must be 0.01..999999999.99");
            }
        }

        String bulkId = UUID.randomUUID().toString();
        bulkPurchaseRepository.save(new BulkPurchase(bulkId, request.platformName(), request.placementDate()));
        List<String> lineIds = request.lines().stream().map(line -> {
            String lineId = UUID.randomUUID().toString();
            bulkLineRepository.save(new BulkPurchaseLine(
                    lineId, bulkId, line.itemRef(), line.purchasedQty(), line.unitCost()));
            return lineId;
        }).toList();
        return Result.ok(new BulkPurchaseResponse(bulkId, lineIds));
    }

    @Transactional
    public Result<Boolean> markReceived(String bulkLineId, int receivedQty) {
        if (receivedQty < 0) {
            return Result.validation("INVALID_RECEIVED_QTY", "receivedQty must be >= 0");
        }
        BulkPurchaseLine line = bulkLineRepository.findById(bulkLineId).orElse(null);
        if (line == null) {
            return Result.domain("BULK_LINE_NOT_FOUND", "Bulk line not found");
        }
        line.setReceivedQty(receivedQty);
        bulkLineRepository.save(line);
        outboxWriter.enqueue(line.getBulkId(), Topics.BULK_RECEIVED,
                new Events.BulkPurchaseReceived(line.getBulkId(), bulkLineId, receivedQty));
        return Result.ok(Boolean.TRUE);
    }

    @Transactional
    public Result<AllocationResponse> allocate(AllocateRequest request) {
        int qty = request.quantity();
        if (qty < 1) {
            return Result.validation("INVALID_ALLOC_QTY", "Allocation quantity must be >= 1");
        }
        BulkPurchaseLine bulkLine = bulkLineRepository.findById(request.bulkLineId()).orElse(null);
        if (bulkLine == null) {
            return Result.domain("BULK_LINE_NOT_FOUND", "Bulk line not found");
        }
        OrderLineAllocationState orderLine = orderLineStateRepository.findById(request.orderLineId()).orElse(null);
        if (orderLine == null) {
            // register from request snapshot if provided
            if (request.orderId() == null || request.requestedQty() < 1) {
                return Result.domain("ORDER_LINE_NOT_FOUND", "Order line allocation state not found");
            }
            orderLine = new OrderLineAllocationState(request.orderLineId(), request.orderId(), request.requestedQty());
            orderLineStateRepository.save(orderLine);
        }

        int bulkAllocated = allocationRepository.totalForBulkLine(request.bulkLineId());
        int orderAllocated = allocationRepository.totalForOrderLine(request.orderLineId());
        if (qty > bulkLine.getReceivedQty() - bulkAllocated) {
            return Result.validation("OVER_ALLOC_BULK", "Allocation exceeds remaining received bulk quantity");
        }
        if (qty > orderLine.getRequestedQty() - orderAllocated) {
            return Result.validation("OVER_ALLOC_ORDER", "Allocation exceeds remaining requested quantity");
        }

        String allocationId = UUID.randomUUID().toString();
        allocationRepository.save(new Allocation(
                allocationId, request.bulkLineId(), request.orderLineId(), orderLine.getOrderId(), qty));
        orderLine.addAllocated(qty);
        orderLineStateRepository.save(orderLine);

        List<OrderLineAllocationState> orderLines = orderLineStateRepository.findByOrderId(orderLine.getOrderId());
        boolean allFully = !orderLines.isEmpty() && orderLines.stream().allMatch(OrderLineAllocationState::isFullyAllocated);
        if (allFully) {
            outboxWriter.enqueue(orderLine.getOrderId(), Topics.ORDERS_FULLY_ALLOCATED,
                    new Events.OrderFullyAllocated(orderLine.getOrderId(), System.currentTimeMillis()));
        }
        return Result.ok(new AllocationResponse(allocationId, qty, orderLine.isFullyAllocated(), allFully));
    }

    public int totalAllocatedForBulkLine(String bulkLineId) {
        return allocationRepository.totalForBulkLine(bulkLineId);
    }

    public int totalAllocatedForOrderLine(String orderLineId) {
        return allocationRepository.totalForOrderLine(orderLineId);
    }

    public record BulkLineInput(String itemRef, int purchasedQty, BigDecimal unitCost) {}
    public record BulkPurchaseRequest(String platformName, LocalDate placementDate, List<BulkLineInput> lines) {}
    public record BulkPurchaseResponse(String bulkId, List<String> bulkLineIds) {}
    public record AllocateRequest(String bulkLineId, String orderLineId, String orderId, int requestedQty, int quantity) {}
    public record AllocationResponse(String allocationId, int quantity, boolean orderLineFullyAllocated, boolean orderFullyAllocated) {}
}
