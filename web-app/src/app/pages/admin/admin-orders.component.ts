import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute } from '@angular/router';
import { ApiService, OrderDetail, OrderSummary } from '../../core/api.service';
import { CatalogDeal, CatalogService } from '../../core/catalog.service';
import { enrichOrderForDisplay } from '../../core/order-display';
import { labelSource, labelStatus, randZar, shortId } from './admin-format';

type StatusKey = 'Created' | 'Allocated' | 'Delivered_To_Customer';
type FilterKey = 'all' | 'WhatsApp' | StatusKey;

@Component({
  selector: 'app-admin-orders',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './admin-orders.component.html',
  styleUrl: './admin-orders.component.css',
})
export class AdminOrdersComponent implements OnInit {
  private readonly api = inject(ApiService);
  private readonly catalog = inject(CatalogService);
  private readonly route = inject(ActivatedRoute);

  orders = signal<OrderSummary[]>([]);
  selected = signal<OrderDetail | null>(null);
  message = signal('');
  error = signal('');
  filter = signal<FilterKey>('all');
  private catalogDeals: CatalogDeal[] = [];

  readonly filtered = computed(() => {
    const f = this.filter();
    const list = this.orders();
    if (f === 'all') return list;
    if (f === 'WhatsApp') return list.filter((o) => (o.source || 'Web') === 'WhatsApp');
    return list.filter((o) => o.orderStatus === f);
  });

  readonly rand = randZar;
  readonly labelStatus = labelStatus;
  readonly labelSource = labelSource;
  readonly shortId = shortId;

  ngOnInit(): void {
    this.catalog.loadDeals().subscribe((deals) => {
      this.catalogDeals = deals;
      const sel = this.selected();
      if (sel) this.selected.set(enrichOrderForDisplay(sel, this.catalogDeals));
    });

    this.route.queryParamMap.subscribe((params) => {
      const f = params.get('filter');
      if (f === 'WhatsApp' || f === 'Created' || f === 'Allocated' || f === 'Delivered_To_Customer') {
        this.filter.set(f);
      }
      const openId = params.get('open');
      if (openId) {
        this.open(openId);
      }
    });

    this.refresh();
  }

  refresh(): void {
    this.error.set('');
    this.api.listOrders().subscribe({
      next: (orders) => {
        this.orders.set(orders ?? []);
        const sel = this.selected();
        const openId = this.route.snapshot.queryParamMap.get('open');
        if (openId) {
          this.open(openId);
        } else if (sel) {
          this.open(sel.orderId);
        } else if (orders?.length) {
          this.open(orders[orders.length - 1].orderId);
        }
      },
      error: (err) => this.error.set(err?.error?.message ?? 'Failed to load orders'),
    });
  }

  open(orderId: string): void {
    this.error.set('');
    this.api.getOrder(orderId).subscribe({
      next: (detail) => this.selected.set(enrichOrderForDisplay(detail, this.catalogDeals)),
      error: (err) => this.error.set(err?.error?.message ?? 'Order not found'),
    });
  }

  eta(status?: string): string {
    switch (status) {
      case 'Created':
        return 'Tell customer: about 3–5 days';
      case 'Allocated':
        return 'Tell customer: delivery in 1–3 days';
      case 'Delivered_To_Customer':
        return 'Customer has the order';
      default:
        return '';
    }
  }

  setStatus(orderId: string, status: StatusKey): void {
    this.message.set('');
    this.error.set('');
    this.api.updateStatus(orderId, status).subscribe({
      next: () => {
        this.message.set(
          status === 'Created'
            ? 'Marked as new order.'
            : status === 'Allocated'
              ? 'Marked as bought/packed.'
              : 'Status updated.'
        );
        this.refresh();
      },
      error: (err) => this.error.set(err?.error?.message ?? 'Status update failed'),
    });
  }

  deliver(orderId: string): void {
    this.message.set('');
    this.error.set('');
    this.api.markDelivered(orderId).subscribe({
      next: () => {
        this.message.set('Marked delivered.');
        this.refresh();
      },
      error: (err) => this.error.set(err?.error?.message ?? 'Deliver failed'),
    });
  }

  copyLink(url: string): void {
    navigator.clipboard?.writeText(url).then(
      () => this.message.set('Product link copied'),
      () => this.message.set(url)
    );
  }
}
