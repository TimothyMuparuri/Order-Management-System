import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { ApiService, OrderSummary } from '../../core/api.service';
import { labelSource, labelStatus, randZar, shortId } from './admin-format';

@Component({
  selector: 'app-admin-overview',
  standalone: true,
  imports: [RouterLink],
  templateUrl: './admin-overview.component.html',
  styleUrl: './admin-overview.component.css',
})
export class AdminOverviewComponent implements OnInit {
  private readonly api = inject(ApiService);

  readonly orders = signal<OrderSummary[]>([]);
  readonly error = signal('');
  readonly loading = signal(true);

  readonly stats = computed(() => {
    const list = this.orders();
    const total = list.length;
    const neu = list.filter((o) => o.orderStatus === 'Created').length;
    const packed = list.filter((o) => o.orderStatus === 'Allocated').length;
    const delivered = list.filter((o) => o.orderStatus === 'Delivered_To_Customer').length;
    const whatsapp = list.filter((o) => (o.source || 'Web') === 'WhatsApp').length;
    const revenue = list.reduce((sum, o) => sum + (Number(o.orderTotal) || 0), 0);
    return { total, neu, packed, delivered, whatsapp, revenue };
  });

  readonly recent = computed(() => [...this.orders()].reverse().slice(0, 6));

  readonly rand = randZar;
  readonly labelStatus = labelStatus;
  readonly labelSource = labelSource;
  readonly shortId = shortId;

  ngOnInit(): void {
    this.reload();
  }

  reload(): void {
    this.loading.set(true);
    this.error.set('');
    this.api.listOrders().subscribe({
      next: (orders) => {
        this.orders.set(orders ?? []);
        this.loading.set(false);
      },
      error: (err) => {
        this.error.set(err?.error?.message ?? 'Failed to load dashboard');
        this.loading.set(false);
      },
    });
  }
}
