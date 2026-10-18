import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

export interface OrderSummary {
  orderId: string;
  customerId?: string;
  customerName?: string;
  phone?: string;
  source?: string;
  orderStatus: string;
  orderTotal?: number;
}

export interface OrderLineView {
  orderLineId: string;
  itemRef: string;
  requestedQty: number;
  fullyAllocated: boolean;
  productName?: string;
  storeName?: string;
  productUrl?: string;
  imageUrl?: string;
  productSize?: string;
  unitPrice?: number;
  lineTotal?: number;
}

export interface OrderDetail {
  orderId: string;
  customerId: string;
  customerName?: string;
  email?: string;
  phone?: string;
  address?: string;
  source?: string;
  orderStatus: string;
  orderTotal?: number;
  lines: OrderLineView[];
}

export interface AdminCreateOrderRequest {
  customerId: string;
  customerName: string;
  phone: string;
  email?: string;
  address?: string;
  source?: string;
  lines: OrderLineInput[];
}

export interface OrderLineInput {
  searchTerm?: string;
  imageId?: string;
  itemRef?: string;
  productName?: string;
  storeName?: string;
  productUrl?: string;
  imageUrl?: string;
  productSize?: string;
  unitPrice?: number;
  quantity: number;
}

export interface Candidate {
  itemRef: string;
  confidence: number;
}

@Injectable({ providedIn: 'root' })
export class ApiService {
  private readonly http = inject(HttpClient);
  private readonly base = environment.apiBaseUrl;

  submitOrder(
    customerId: string,
    customerName: string,
    lines: OrderLineInput[],
    contact?: { email?: string; phone?: string; address?: string }
  ): Observable<OrderDetail> {
    return this.http.post<OrderDetail>(`${this.base}/api/orders`, {
      customerId,
      customerName,
      email: contact?.email,
      phone: contact?.phone,
      address: contact?.address,
      lines,
    });
  }

  uploadImage(file: File): Observable<{ imageId: string; objectKey: string }> {
    const form = new FormData();
    form.append('file', file);
    return this.http.post<{ imageId: string; objectKey: string }>(`${this.base}/api/images`, form);
  }

  /** Customer-facing track (order-service). */
  trackOrder(orderId: string): Observable<OrderDetail> {
    return this.http.get<OrderDetail>(`${this.base}/api/orders/${orderId}`);
  }

  listOrders(): Observable<OrderSummary[]> {
    return this.http.get<OrderSummary[]>(`${this.base}/api/admin/orders`);
  }

  /** Admin intake (WhatsApp / manual) — same create path as the website. */
  createAdminOrder(body: AdminCreateOrderRequest): Observable<OrderDetail> {
    return this.http.post<OrderDetail>(`${this.base}/api/admin/orders`, body);
  }

  getOrder(orderId: string): Observable<OrderDetail> {
    return this.http.get<OrderDetail>(`${this.base}/api/admin/orders/${orderId}`);
  }

  updateStatus(orderId: string, status: string): Observable<unknown> {
    return this.http.put(`${this.base}/api/admin/orders/${orderId}/status`, { status });
  }

  recordBulkPurchase(body: unknown): Observable<unknown> {
    return this.http.post(`${this.base}/api/admin/bulk-purchases`, body);
  }

  markReceived(bulkLineId: string, receivedQty: number): Observable<unknown> {
    return this.http.post(`${this.base}/api/admin/bulk-lines/${bulkLineId}/received`, { receivedQty });
  }

  allocate(body: unknown): Observable<unknown> {
    return this.http.post(`${this.base}/api/admin/allocations`, body);
  }

  markDelivered(orderId: string): Observable<unknown> {
    return this.http.post(`${this.base}/api/admin/orders/${orderId}/delivered`, {});
  }
}
