import { request } from './request'

export async function uploadImageApi(file: File, kind: 'products' | 'avatars'): Promise<string> {
  const data = new FormData()
  data.append('file', file)
  data.append('kind', kind)
  const result = await request<{ url: string }>({
    url: '/api/uploads/images',
    method: 'post',
    data,
    headers: { 'Content-Type': undefined },
    timeout: 30000,
  })
  return result.url
}
