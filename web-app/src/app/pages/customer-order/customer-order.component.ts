import { Component, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ApiService, OrderLineInput } from '../../core/api.service';

interface LineDraft {
  searchTerm: string;
  quantity: number;
  imageId?: string;
  itemRef?: string;
  candidates: { itemRef: string; confidence: number }[];
}

@Component({
  selector: 'app-customer-order',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './customer-order.component.html',
  styleUrl: './customer-order.component.css',
})
export class CustomerOrderComponent {
  customerId = 'customer-demo-1';
  lines = signal<LineDraft[]>([{ searchTerm: '', quantity: 1, candidates: [] }]);
  message = signal('');
  error = signal('');

  constructor(private readonly api: ApiService) {}

  addLine(): void {
    if (this.lines().length >= 100) {
      this.error.set('Maximum 100 lines');
      return;
    }
    this.lines.update((l) => [...l, { searchTerm: '', quantity: 1, candidates: [] }]);
  }

  removeLine(index: number): void {
    this.lines.update((l) => l.filter((_, i) => i !== index));
  }

  onImageSelected(index: number, event: Event): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];
    if (!file) return;
    this.api.uploadImage(file).subscribe({
      next: (res) => {
        this.lines.update((lines) =>
          lines.map((line, i) =>
            i === index
              ? {
                  ...line,
                  imageId: res.imageId,
                  candidates: [
                    { itemRef: `sku-stub-${res.imageId.slice(0, 8)}`, confidence: 0.91 },
                    { itemRef: `sku-alt-${res.imageId.slice(0, 8)}`, confidence: 0.72 },
                  ],
                }
              : line
          )
        );
        this.message.set(`Image uploaded: ${res.imageId}`);
      },
      error: (err) => this.error.set(err?.error?.message ?? 'Image upload failed'),
    });
  }

  confirmMatch(index: number, itemRef: string): void {
    this.lines.update((lines) =>
      lines.map((line, i) => (i === index ? { ...line, itemRef } : line))
    );
  }

  submit(): void {
    this.error.set('');
    this.message.set('');
    const payload: OrderLineInput[] = this.lines().map((l) => ({
      searchTerm: l.searchTerm || undefined,
      imageId: l.imageId,
      itemRef: l.itemRef,
      quantity: l.quantity,
    }));
    this.api.submitOrder(this.customerId, payload).subscribe({
      next: (res: any) => this.message.set(`Order created: ${res.orderId ?? JSON.stringify(res)}`),
      error: (err) => this.error.set(err?.error?.message ?? 'Submit failed'),
    });
  }
}
