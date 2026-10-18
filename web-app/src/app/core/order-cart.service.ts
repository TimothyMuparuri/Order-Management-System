import { Injectable, computed, signal } from '@angular/core';
import { defaultCategoryHint, needsSize, sizeOptionsFor } from './product-sizes';

export interface CartLine {
  id: string;
  productName: string;
  store?: string;
  productUrl?: string;
  imageUrl?: string;
  imageId?: string;
  category?: string;
  /** Selected size (e.g. M, 7). Required when needsSize(category). */
  size?: string;
  /** Unit price in ZAR (Rands). */
  unitPrice?: number;
  quantity: number;
}

const STORAGE_KEY = 'oms.orderCart.v3';

@Injectable({ providedIn: 'root' })
export class OrderCartService {
  readonly lines = signal<CartLine[]>(this.read());

  readonly itemCount = computed(() =>
    this.lines().reduce((sum, line) => sum + line.quantity, 0)
  );

  readonly subtotal = computed(() =>
    this.lines().reduce((sum, line) => sum + this.lineTotal(line), 0)
  );

  addProduct(input: {
    productName: string;
    store?: string;
    productUrl?: string;
    imageUrl?: string | null;
    unitPrice?: number | null;
    category?: string | null;
    size?: string | null;
    quantity?: number;
  }): void {
    const name = input.productName.trim();
    if (!name) return;

    const category =
      input.category || defaultCategoryHint(input.store, name) || undefined;
    const size = input.size?.trim() || undefined;

    const cleaned = this.lines().filter((l) => l.productName.trim().length > 0);

    // Same product + same size merges; different sizes stay separate.
    const key = this.keyOf(name, input.store, input.productUrl, size);
    const existing = cleaned.find(
      (l) => this.keyOf(l.productName, l.store, l.productUrl, l.size) === key
    );
    if (existing) {
      this.persist(
        cleaned.map((l) =>
          l.id === existing.id
            ? {
                ...l,
                quantity: l.quantity + (input.quantity ?? 1),
                imageUrl: l.imageUrl || input.imageUrl || undefined,
                unitPrice: l.unitPrice ?? input.unitPrice ?? undefined,
                category: l.category || category,
              }
            : l
        )
      );
      return;
    }

    const next: CartLine = {
      id: crypto.randomUUID(),
      productName: name,
      store: input.store,
      productUrl: input.productUrl,
      imageUrl: input.imageUrl || undefined,
      unitPrice: input.unitPrice ?? undefined,
      category,
      size,
      quantity: input.quantity ?? 1,
    };
    this.persist([...cleaned, next]);
  }

  remove(id: string): void {
    this.persist(this.lines().filter((l) => l.id !== id));
  }

  updateQuantity(id: string, quantity: number): void {
    const qty = Math.max(1, Math.min(10_000, Math.floor(quantity) || 1));
    this.persist(this.lines().map((l) => (l.id === id ? { ...l, quantity: qty } : l)));
  }

  updateSize(id: string, size: string): void {
    this.persist(this.lines().map((l) => (l.id === id ? { ...l, size: size.trim() } : l)));
  }

  updateLine(id: string, patch: Partial<CartLine>): void {
    this.persist(this.lines().map((l) => (l.id === id ? { ...l, ...patch } : l)));
  }

  clear(): void {
    this.persist([]);
  }

  lineTotal(line: CartLine): number {
    if (line.unitPrice == null || Number.isNaN(line.unitPrice)) {
      return 0;
    }
    return line.unitPrice * line.quantity;
  }

  lineNeedsSize(line: CartLine): boolean {
    return needsSize(line.category);
  }

  sizesFor(line: CartLine): string[] {
    return sizeOptionsFor(line.category);
  }

  missingSizes(): CartLine[] {
    return this.lines().filter((l) => this.lineNeedsSize(l) && !l.size?.trim());
  }

  private keyOf(name: string, store?: string, productUrl?: string, size?: string): string {
    const sizePart = size || '_';
    if (productUrl) {
      return `url:${productUrl}|size:${sizePart}`;
    }
    return `name:${name.toLowerCase()}|${(store || '').toLowerCase()}|size:${sizePart}`;
  }

  private persist(lines: CartLine[]): void {
    this.lines.set(lines);
    try {
      sessionStorage.setItem(STORAGE_KEY, JSON.stringify(lines));
      sessionStorage.removeItem('oms.orderCart.v1');
      sessionStorage.removeItem('oms.orderCart.v2');
    } catch {
      /* ignore */
    }
  }

  private read(): CartLine[] {
    try {
      const raw = sessionStorage.getItem(STORAGE_KEY) || sessionStorage.getItem('oms.orderCart.v2');
      if (!raw) {
        return [];
      }
      const parsed = JSON.parse(raw) as CartLine[];
      if (!Array.isArray(parsed)) {
        return [];
      }
      return parsed
        .filter((l) => l && typeof l.productName === 'string' && l.productName.trim())
        .map((l) => ({
          ...l,
          category: l.category || defaultCategoryHint(l.store, l.productName),
        }));
    } catch {
      return [];
    }
  }
}
