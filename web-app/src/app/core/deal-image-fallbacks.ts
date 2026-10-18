/**
 * Reliable placeholder product photos when retailer scrape fails / is blocked.
 * Keys are catalog deal ids from admin-bff catalog-seed.json.
 * Order matches web-app storefront DEALS[] for local bootstrap.
 */
export const DEAL_IDS_IN_ORDER = [
  'zara-textured-sweatshirt',
  'sm-cameos-tan',
  'takealot-rhaf09s',
  'edgars-good-girl-elixir',
  'yuppiechef-le-creuset-skillet',
  'jet-knit-skirt',
  'yuppiechef-wmf-diadem',
  'dischem-boss-bottled',
  'superbalist-boohoo-maxi',
  'sportscene-vans-knu',
  'takealot-hp-g9',
  'sm-bassett',
  'mrp-puffer',
  'yuppiechef-stanley-mug',
  'edgars-citrus-noir',
  'superbalist-mango-perla',
] as const;

export const DEAL_IMAGE_FALLBACKS: Record<string, string> = {
  'zara-textured-sweatshirt':
    'https://images.unsplash.com/photo-1556821840-3a63f95609a7?auto=format&fit=crop&w=800&q=80',
  'sm-cameos-tan':
    'https://images.unsplash.com/photo-1549298916-b41d501d3772?auto=format&fit=crop&w=800&q=80',
  'takealot-rhaf09s':
    'https://images.unsplash.com/photo-1585515320310-259814833e62?auto=format&fit=crop&w=800&q=80',
  'edgars-good-girl-elixir':
    'https://images.unsplash.com/photo-1541643600914-78b084683601?auto=format&fit=crop&w=800&q=80',
  'yuppiechef-le-creuset-skillet':
    'https://images.unsplash.com/photo-1556910103-1c02745aae4d?auto=format&fit=crop&w=800&q=80',
  'jet-knit-skirt':
    'https://images.unsplash.com/photo-1583496661160-fb5886a0aaaa?auto=format&fit=crop&w=800&q=80',
  'yuppiechef-wmf-diadem':
    'https://images.unsplash.com/photo-1556909114-f6e7ad7d3136?auto=format&fit=crop&w=800&q=80',
  'dischem-boss-bottled':
    'https://images.unsplash.com/photo-1594035910387-fea47794261f?auto=format&fit=crop&w=800&q=80',
  'superbalist-boohoo-maxi':
    'https://images.unsplash.com/photo-1595777457583-95e059d581b8?auto=format&fit=crop&w=800&q=80',
  'sportscene-vans-knu':
    'https://images.unsplash.com/photo-1525966222134-fcfa99b8ae77?auto=format&fit=crop&w=800&q=80',
  'takealot-hp-g9':
    'https://images.unsplash.com/photo-1496181133206-80ce9b88a853?auto=format&fit=crop&w=800&q=80',
  'sm-bassett':
    'https://images.unsplash.com/photo-1606107557195-0e29a4b5b4aa?auto=format&fit=crop&w=800&q=80',
  'mrp-puffer':
    'https://images.unsplash.com/photo-1544022613-e87ca75a784a?auto=format&fit=crop&w=800&q=80',
  'yuppiechef-stanley-mug':
    'https://images.unsplash.com/photo-1514228742587-6b1558fcca3d?auto=format&fit=crop&w=800&q=80',
  'edgars-citrus-noir':
    'https://images.unsplash.com/photo-1547887537-6158d64c35b0?auto=format&fit=crop&w=800&q=80',
  'superbalist-mango-perla':
    'https://images.unsplash.com/photo-1566174053879-31528523f8ae?auto=format&fit=crop&w=800&q=80',
};

/** Category-level fallback when a deal id is unknown. */
export const CATEGORY_IMAGE_FALLBACKS: Record<string, string> = {
  Fashion: DEAL_IMAGE_FALLBACKS['zara-textured-sweatshirt'],
  Shoes: DEAL_IMAGE_FALLBACKS['sm-bassett'],
  Kitchen: DEAL_IMAGE_FALLBACKS['yuppiechef-le-creuset-skillet'],
  Fragrance: DEAL_IMAGE_FALLBACKS['edgars-good-girl-elixir'],
  Tech: DEAL_IMAGE_FALLBACKS['takealot-hp-g9'],
};

export function fallbackImageFor(deal: { id?: string; category?: string; name?: string }): string | null {
  if (deal.id && DEAL_IMAGE_FALLBACKS[deal.id]) {
    return DEAL_IMAGE_FALLBACKS[deal.id];
  }
  if (deal.category && CATEGORY_IMAGE_FALLBACKS[deal.category]) {
    return CATEGORY_IMAGE_FALLBACKS[deal.category];
  }
  return null;
}
