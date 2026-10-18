import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { ApiService, OrderLineInput } from '../../core/api.service';
import { environment } from '../../../environments/environment';

interface WaLineDraft {
  productName: string;
  storeName: string;
  productUrl: string;
  productSize: string;
  unitPrice: string;
  quantity: number;
  imageId?: string;
  imageUrl?: string | null;
  previewUrl?: string | null;
  uploading?: boolean;
}

@Component({
  selector: 'app-admin-whatsapp',
  standalone: true,
  imports: [FormsModule, RouterLink],
  templateUrl: './admin-whatsapp.component.html',
  styleUrl: './admin-whatsapp.component.css',
})
export class AdminWhatsappComponent {
  private readonly api = inject(ApiService);
  private readonly router = inject(Router);

  message = signal('');
  error = signal('');
  saving = signal(false);

  waName = '';
  waPhone = '';
  waEmail = '';
  waAddress = '';
  waLines: WaLineDraft[] = [this.emptyLine()];

  addLine(): void {
    this.waLines = [...this.waLines, this.emptyLine()];
  }

  removeLine(index: number): void {
    if (this.waLines.length <= 1) return;
    const line = this.waLines[index];
    if (line.previewUrl?.startsWith('blob:')) {
      URL.revokeObjectURL(line.previewUrl);
    }
    this.waLines = this.waLines.filter((_, i) => i !== index);
  }

  onImageSelected(index: number, event: Event): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];
    if (!file) return;

    if (!file.type.startsWith('image/')) {
      this.error.set('Please upload a JPEG or PNG photo');
      return;
    }

    const line = this.waLines[index];
    if (line.previewUrl?.startsWith('blob:')) {
      URL.revokeObjectURL(line.previewUrl);
    }
    const preview = URL.createObjectURL(file);
    this.waLines = this.waLines.map((l, i) =>
      i === index ? { ...l, previewUrl: preview, uploading: true, imageId: undefined, imageUrl: undefined } : l
    );
    this.error.set('');

    this.api.uploadImage(file).subscribe({
      next: (res) => {
        const publicUrl = `${environment.apiBaseUrl}/api/images/${res.imageId}`;
        this.waLines = this.waLines.map((l, i) =>
          i === index
            ? {
                ...l,
                imageId: res.imageId,
                imageUrl: publicUrl,
                previewUrl: preview,
                uploading: false,
              }
            : l
        );
        this.message.set('Photo uploaded for line ' + (index + 1));
      },
      error: (err) => {
        this.waLines = this.waLines.map((l, i) =>
          i === index ? { ...l, uploading: false, previewUrl: null, imageId: undefined, imageUrl: undefined } : l
        );
        this.error.set(err?.error?.message ?? 'Image upload failed');
      },
    });

    input.value = '';
  }

  clearImage(index: number): void {
    const line = this.waLines[index];
    if (line.previewUrl?.startsWith('blob:')) {
      URL.revokeObjectURL(line.previewUrl);
    }
    this.waLines = this.waLines.map((l, i) =>
      i === index ? { ...l, imageId: undefined, imageUrl: undefined, previewUrl: null, uploading: false } : l
    );
  }

  submit(): void {
    this.message.set('');
    this.error.set('');
    const name = this.waName.trim();
    const phone = this.waPhone.trim();
    if (!name) {
      this.error.set('Customer name is required');
      return;
    }
    if (!phone) {
      this.error.set('WhatsApp / phone number is required');
      return;
    }
    if (this.waLines.some((l) => l.uploading)) {
      this.error.set('Wait for photo uploads to finish');
      return;
    }

    const lines: OrderLineInput[] = [];
    for (const [i, line] of this.waLines.entries()) {
      const productName = line.productName.trim();
      if (!productName && !line.imageId) {
        this.error.set(`Line ${i + 1}: product name or photo is required`);
        return;
      }
      const qty = Number(line.quantity) || 1;
      if (qty < 1) {
        this.error.set(`Line ${i + 1}: quantity must be at least 1`);
        return;
      }
      const priceRaw = line.unitPrice.trim();
      const unitPrice = priceRaw ? Number(priceRaw) : undefined;
      if (priceRaw && (unitPrice == null || Number.isNaN(unitPrice))) {
        this.error.set(`Line ${i + 1}: price must be a number`);
        return;
      }
      const store = line.storeName.trim();
      const url = line.productUrl.trim();
      const size = line.productSize.trim();
      const label = productName || 'WhatsApp photo item';
      lines.push({
        productName: label,
        storeName: store || undefined,
        productUrl: url || undefined,
        productSize: size || undefined,
        unitPrice,
        quantity: qty,
        imageId: line.imageId,
        imageUrl: line.imageUrl || undefined,
        searchTerm: `${label}${store ? ` (${store})` : ''}${size ? ` size ${size}` : ''}${url ? ` — ${url}` : ''}`.slice(0, 200),
      });
    }

    const customerId = `wa-${crypto.randomUUID().slice(0, 8)}`;
    this.saving.set(true);
    this.api
      .createAdminOrder({
        customerId,
        customerName: name,
        phone,
        email: this.waEmail.trim() || undefined,
        address: this.waAddress.trim() || undefined,
        source: 'WhatsApp',
        lines,
      })
      .subscribe({
        next: (created) => {
          this.saving.set(false);
          this.message.set(`Order created · #${created.orderId.slice(0, 8)}`);
          this.reset();
          this.router.navigate(['/admin/orders'], {
            queryParams: { filter: 'WhatsApp', open: created.orderId },
          });
        },
        error: (err) => {
          this.saving.set(false);
          this.error.set(err?.error?.message ?? err?.error?.error ?? 'Failed to create WhatsApp order');
        },
      });
  }

  private emptyLine(): WaLineDraft {
    return {
      productName: '',
      storeName: '',
      productUrl: '',
      productSize: '',
      unitPrice: '',
      quantity: 1,
      imageId: undefined,
      imageUrl: undefined,
      previewUrl: null,
      uploading: false,
    };
  }

  private reset(): void {
    for (const line of this.waLines) {
      if (line.previewUrl?.startsWith('blob:')) {
        URL.revokeObjectURL(line.previewUrl);
      }
    }
    this.waName = '';
    this.waPhone = '';
    this.waEmail = '';
    this.waAddress = '';
    this.waLines = [this.emptyLine()];
  }
}
