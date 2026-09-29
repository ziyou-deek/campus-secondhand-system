import type { PageResult } from './types'

export function normalizePage<T>(data: PageResult<T> | T[] | null | undefined): {
  list: T[]
  total: number
} {
  if (!data) return { list: [], total: 0 }
  if (Array.isArray(data)) return { list: data, total: data.length }
  const list = data.list || data.records || []
  return { list, total: data.total ?? list.length }
}

export function productCover(product: {
  cover?: string
  images?: string[] | string
}): string {
  if (product.cover) return product.cover
  if (Array.isArray(product.images) && product.images.length) return product.images[0]
  if (typeof product.images === 'string' && product.images) {
    try {
      const parsed = JSON.parse(product.images)
      if (Array.isArray(parsed) && parsed[0]) return parsed[0]
    } catch {
      return product.images.split(',')[0]
    }
  }
  return 'https://via.placeholder.com/300x200?text=No+Image'
}

export function productImages(product: { cover?: string; images?: string[] | string }): string[] {
  if (Array.isArray(product.images)) return product.images.filter(Boolean)
  if (typeof product.images === 'string' && product.images) {
    try {
      const parsed = JSON.parse(product.images)
      if (Array.isArray(parsed)) return parsed.filter((item): item is string => typeof item === 'string' && !!item)
    } catch {
      return product.images.split(',').map((item) => item.trim()).filter(Boolean)
    }
  }
  return product.cover ? [product.cover] : []
}
