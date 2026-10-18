export function randZar(n?: number | null): string {
  if (n == null || Number.isNaN(Number(n))) return '—';
  return `R${Math.round(Number(n)).toLocaleString('en-ZA')}`;
}

export function labelStatus(status?: string): string {
  switch (status) {
    case 'Created':
      return 'New — buy items';
    case 'Allocated':
      return 'Bought / packed';
    case 'Delivered_To_Customer':
      return 'Delivered';
    default:
      return status || 'Unknown';
  }
}

export function labelSource(source?: string): string {
  return source === 'WhatsApp' ? 'WhatsApp' : 'Web';
}

export function shortId(id: string): string {
  return id.slice(0, 8);
}
