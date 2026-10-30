import http from './http'

export const createTicket = (data: Record<string, unknown>) => http.post('/tickets', data)

export const listTickets = (params: Record<string, unknown>) => http.get('/tickets', { params })

export const getTicket = (id: number | string) => http.get(`/tickets/${id}`)

export const assignTicket = (id: number | string, workerId: number) =>
  http.put(`/tickets/${id}/assign`, { workerId })

export const acceptTicket = (id: number | string) => http.put(`/tickets/${id}/accept`)

export const startTicket = (id: number | string) => http.put(`/tickets/${id}/start`)

export const finishTicket = (id: number | string, result: string, feeAmount?: number) =>
  http.put(`/tickets/${id}/finish`, { result, feeAmount })

export const confirmTicket = (id: number | string) => http.put(`/tickets/${id}/confirm`)

export const reworkTicket = (id: number | string, reason: string) =>
  http.put(`/tickets/${id}/rework`, { reason })

export const returnTicket = (id: number | string, reason: string) =>
  http.put(`/tickets/${id}/return`, { reason })

export const cancelTicket = (id: number | string, reason: string) =>
  http.put(`/tickets/${id}/cancel`, { reason })

export const urgeTicket = (id: number | string) => http.post(`/tickets/${id}/urge`)

export const uploadFile = (file: File) => {
  const form = new FormData()
  form.append('file', file)
  return http.post('/upload', form)
}
