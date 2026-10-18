import { Component, computed, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { ApiService, OrderLineInput } from '../../core/api.service';
import { OrderCartService } from '../../core/order-cart.service';

@Component({
  selector: 'app-customer-order',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './customer-order.component.html',
  styleUrl: './customer-order.component.css',
})
export class CustomerOrderComponent {
  private readonly api = inject(ApiService);
  readonly cart = inject(OrderCartService);
  private readonly router = inject(Router);

  readonly lines = this.cart.lines;
  readonly itemCount = this.cart.itemCount;
  readonly subtotal = this.cart.subtotal;
  readonly isEmpty = computed(() => this.lines().length === 0);

  customerId = '';
  customerName = '';
  phone = '';
  email = '';
  address = '';
  message = signal('');
  error = signal('');
  created = signal<{
    orderId: string;
    customerId: string;
    customerName: string;
    phone?: string;
    email?: string;
    address?: string;
    orderStatus?: string;
    orderTotal?: number;
    lines?: unknown[];
  } | null>(null);

  constructor() {
    const savedId = sessionStorage.getItem('oms.customerId');
    const savedName = sessionStorage.getItem('oms.customerName');
    const savedPhone = sessionStorage.getItem('oms.customerPhone');
    const savedEmail = sessionStorage.getItem('oms.customerEmail');
    const savedAddress = sessionStorage.getItem('oms.customerAddress');
    this.customerId = savedId || `cust-${crypto.randomUUID().slice(0, 8)}`;
    this.customerName = savedName || '';
    this.phone = savedPhone || '';
    this.email = savedEmail || '';
    this.address = savedAddress || '';
  }

  rand(amount: number | undefined | null): string {
    if (amount == null || Number.isNaN(amount)) {
      return '—';
    }
    return `R${Math.round(amount).toLocaleString('en-ZA')}`;
  }

  keepShopping(): void {
    this.router.navigate(['/'], { fragment: 'sale' });
  }

  removeLine(id: string): void {
    this.cart.remove(id);
  }

  setQuantity(id: string, value: string | number): void {
    this.cart.updateQuantity(id, Number(value));
  }

  setSize(id: string, size: string): void {
    this.cart.updateSize(id, size);
  }

  onImageSelected(id: string, event: Event): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];
    if (!file) return;

    const preview = URL.createObjectURL(file);
    this.cart.updateLine(id, { imageUrl: preview });

    this.api.uploadImage(file).subscribe({
      next: (res) => this.cart.updateLine(id, { imageId: res.imageId, imageUrl: preview }),
      error: (err) => this.error.set(err?.error?.message ?? 'Image upload failed'),
    });
  }

  submit(): void {
    this.error.set('');
    this.message.set('');
    this.created.set(null);

    if (this.isEmpty()) {
      this.error.set('Your cart is empty. Add products from the shop first.');
      return;
    }

    const name = this.customerName.trim();
    const customerId = this.customerId.trim();
    const phone = this.phone.trim();
    const email = this.email.trim();
    const address = this.address.trim();
    if (!name) {
      this.error.set('Please enter your name');
      return;
    }
    if (!phone) {
      this.error.set('Please enter your phone number so we can contact you about delivery');
      return;
    }
    if (!customerId) {
      this.error.set('Please enter a customer id');
      return;
    }

    const missing = this.cart.missingSizes();
    if (missing.length) {
      this.error.set(
        `Please choose a size for: ${missing.map((m) => m.productName).join(', ')}`
      );
      return;
    }

    const payload: OrderLineInput[] = this.lines().map((l) => ({
      productName: l.productName,
      storeName: l.store,
      productUrl: l.productUrl,
      productSize: l.size,
      searchTerm: l.productUrl
        ? `${l.productName}${l.store ? ` (${l.store})` : ''}${l.size ? ` size ${l.size}` : ''} — ${l.productUrl}`.slice(0, 200)
        : `${l.productName}${l.size ? ` size ${l.size}` : ''}`.slice(0, 200),
      imageId: l.imageId,
      imageUrl: l.imageUrl?.startsWith('blob:') ? undefined : l.imageUrl,
      unitPrice: l.unitPrice,
      quantity: l.quantity,
    }));

    sessionStorage.setItem('oms.customerId', customerId);
    sessionStorage.setItem('oms.customerName', name);
    sessionStorage.setItem('oms.customerPhone', phone);
    if (email) sessionStorage.setItem('oms.customerEmail', email);
    if (address) sessionStorage.setItem('oms.customerAddress', address);

    this.api.submitOrder(customerId, name, payload, { phone, email, address }).subscribe({
      next: (res) => {
        this.created.set({
          orderId: res.orderId,
          customerId: res.customerId ?? customerId,
          customerName: res.customerName ?? name,
          phone: res.phone ?? phone,
          email: res.email || email || undefined,
          address: res.address || address || undefined,
          orderStatus: res.orderStatus,
          orderTotal: res.orderTotal != null ? Number(res.orderTotal) : this.subtotal(),
          lines: res.lines,
        });
        sessionStorage.setItem('oms.lastOrderId', res.orderId);
        this.message.set('Order sent to admin');
        this.cart.clear();
      },
      error: (err) => this.error.set(err?.error?.message ?? 'Submit failed'),
    });
  }
}
