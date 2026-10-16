import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

export interface OrderSummary {
  orderId: string;
  orderStatus: string;
}

export interface OrderLineInput {
  searchTerm?: string;
  imageId?: string;
  itemRef?: string;
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

  submitOrder(customerId: string, lines: OrderLineInput[]): Observable<unknown> {
    return this.http.post(`${this.base}/api/orders`, { customerId, lines });
  }

  uploadImage(file: File): Observable<{ imageId: string; objectKey: string }> {
    const form = new FormData();
    form.append('file', file);
    return this.http.post<{ imageId: string; objectKey: string }>(`${this.base}/api/images`, form);
  }

  listOrders(): Observable<OrderSummary[]> {
    return this.http.get<OrderSummary[]>(`${this.base}/api/admin/orders`);
  }

  getOrder(orderId: string): Observable<unknown> {
    return this.http.get(`${this.base}/api/admin/orders/${orderId}`);
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
