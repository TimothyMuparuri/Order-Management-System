import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ApiService, OrderSummary } from '../../core/api.service';

@Component({
  selector: 'app-admin',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './admin.component.html',
  styleUrl: './admin.component.css',
})
export class AdminComponent implements OnInit {
  orders = signal<OrderSummary[]>([]);
  selected = signal<any>(null);
  message = signal('');
  error = signal('');

  platformName = 'AliExpress';
  placementDate = new Date().toISOString().slice(0, 10);
  itemRef = 'sku-1';
  purchasedQty = 10;
  unitCost = 1.5;
  bulkLineId = '';
  receivedQty = 10;
  orderLineId = '';
  orderIdForAlloc = '';
  requestedQty = 1;
  allocQty = 1;
  statusValue = 'Allocated';

  constructor(private readonly api: ApiService) {}

  ngOnInit(): void {
    this.refresh();
  }

  refresh(): void {
    this.api.listOrders().subscribe({
      next: (orders) => this.orders.set(orders ?? []),
      error: (err) => this.error.set(err?.error?.message ?? 'Failed to load orders'),
    });
  }

  open(orderId: string): void {
    this.api.getOrder(orderId).subscribe({
      next: (detail) => this.selected.set(detail),
      error: (err) => this.error.set(err?.error?.message ?? 'Order not found'),
    });
  }

  updateStatus(orderId: string): void {
    this.api.updateStatus(orderId, this.statusValue).subscribe({
      next: () => {
        this.message.set(`Status updated to ${this.statusValue}`);
        this.refresh();
        this.open(orderId);
      },
      error: (err) => this.error.set(err?.error?.message ?? 'Status update failed'),
    });
  }

  recordBulk(): void {
    this.api
      .recordBulkPurchase({
        platformName: this.platformName,
        placementDate: this.placementDate,
        lines: [{ itemRef: this.itemRef, purchasedQty: this.purchasedQty, unitCost: this.unitCost }],
      })
      .subscribe({
        next: (res: any) => {
          this.message.set(`Bulk recorded: ${res.bulkId}`);
          this.bulkLineId = res.bulkLineIds?.[0] ?? '';
        },
        error: (err) => this.error.set(err?.error?.message ?? 'Bulk record failed'),
      });
  }

  markReceived(): void {
    this.api.markReceived(this.bulkLineId, this.receivedQty).subscribe({
      next: () => this.message.set('Received quantity recorded'),
      error: (err) => this.error.set(err?.error?.message ?? 'Mark received failed'),
    });
  }

  allocate(): void {
    this.api
      .allocate({
        bulkLineId: this.bulkLineId,
        orderLineId: this.orderLineId,
        orderId: this.orderIdForAlloc,
        requestedQty: this.requestedQty,
        quantity: this.allocQty,
      })
      .subscribe({
        next: (res: any) => this.message.set(`Allocated ${res.quantity}`),
        error: (err) => this.error.set(err?.error?.message ?? 'Allocate failed'),
      });
  }

  deliver(orderId: string): void {
    this.api.markDelivered(orderId).subscribe({
      next: () => {
        this.message.set('Order marked delivered');
        this.refresh();
      },
      error: (err) => this.error.set(err?.error?.message ?? 'Deliver failed'),
    });
  }
}
