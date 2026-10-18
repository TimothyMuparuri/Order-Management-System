import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { ApiService, OrderDetail } from '../../core/api.service';
import { CatalogDeal, CatalogService } from '../../core/catalog.service';
import { enrichOrderForDisplay } from '../../core/order-display';

@Component({
  selector: 'app-track-order',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './track-order.component.html',
  styleUrl: './track-order.component.css',
})
export class TrackOrderComponent implements OnInit {
  private readonly api = inject(ApiService);
  private readonly catalog = inject(CatalogService);
  private readonly route = inject(ActivatedRoute);

  orderId = '';
  order = signal<OrderDetail | null>(null);
  error = signal('');
  loading = signal(false);
  private catalogDeals: CatalogDeal[] = [];

  ngOnInit(): void {
    this.catalog.loadDeals().subscribe((deals) => (this.catalogDeals = deals));
    const fromRoute = this.route.snapshot.queryParamMap.get('id')?.trim();
    const saved = sessionStorage.getItem('oms.lastOrderId') || '';
    this.orderId = fromRoute || saved;
    if (this.orderId) {
      this.lookup();
    }
  }

  lookup(): void {
    const id = this.orderId.trim();
    if (!id) {
      this.error.set('Enter your order number');
      return;
    }
    this.loading.set(true);
    this.error.set('');
    this.order.set(null);
    this.api.trackOrder(id).subscribe({
      next: (detail) => {
        this.order.set(enrichOrderForDisplay(detail, this.catalogDeals));
        this.loading.set(false);
        sessionStorage.setItem('oms.lastOrderId', detail.orderId);
      },
      error: () => {
        this.loading.set(false);
        this.error.set('We could not find that order. Check the number and try again.');
      },
    });
  }

  rand(n?: number | null): string {
    if (n == null || Number.isNaN(Number(n))) return '—';
    return `R${Math.round(Number(n)).toLocaleString('en-ZA')}`;
  }

  statusTitle(status?: string): string {
    switch (status) {
      case 'Created':
        return 'Order received';
      case 'Allocated':
        return 'Being packed for delivery';
      case 'Delivered_To_Customer':
        return 'Delivered';
      default:
        return status || 'Unknown';
    }
  }

  statusHelp(status?: string): string {
    switch (status) {
      case 'Created':
        return 'We have your order. Our personal shopper is buying your items from the stores. Expected delivery in about 3–5 days.';
      case 'Allocated':
        return 'Your items are bought and packed. Delivery is usually within 1–3 days.';
      case 'Delivered_To_Customer':
        return 'Your order has been delivered. Thank you for shopping with sapersonalshopper.';
      default:
        return '';
    }
  }

  stepOn(status: string | undefined, step: 'Created' | 'Allocated' | 'Delivered_To_Customer'): boolean {
    if (!status) return false;
    if (step === 'Created') return true;
    if (step === 'Allocated') return status === 'Allocated' || status === 'Delivered_To_Customer';
    return status === 'Delivered_To_Customer';
  }
}
