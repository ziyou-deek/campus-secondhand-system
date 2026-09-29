export function formatPrice(price: number | string | undefined | null): string {
  if (price === undefined || price === null || price === '') return '—'
  const n = Number(price)
  if (Number.isNaN(n)) return String(price)
  return `¥${n.toFixed(2)}`
}

export function formatDate(val: string | number | Date | undefined | null): string {
  if (!val) return '—'
  const d = new Date(val)
  if (Number.isNaN(d.getTime())) return String(val)
  const pad = (n: number) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`
}

/** 商品：0待审核 1在售 2已下架 3已售出 4审核拒绝 5交易中 */
export const productStatusMap: Record<string | number, string> = {
  0: '待审核',
  1: '在售',
  2: '已下架',
  3: '已售出',
  4: '审核拒绝',
  5: '交易中',
  PENDING: '待审核',
  ON_SALE: '在售',
  OFF_SHELF: '已下架',
  SOLD: '已售出',
  REJECTED: '审核拒绝',
}

/** 订单：0待交易 1交易中 2已完成 3已取消 */
export const orderStatusMap: Record<string | number, string> = {
  0: '待交易',
  1: '交易中',
  2: '已完成',
  3: '已取消',
  PENDING: '待交易',
  TRADING: '交易中',
  COMPLETED: '已完成',
  CANCELLED: '已取消',
}
