import http from './http'

export const listTickets = (params: Record<string, unknown>) => http.get('/tickets', { params })

export const getTicket = (id: number | string) => http.get(`/tickets/${id}`)

export const assignTicket = (id: number | string, workerId: number, remark?: string) =>
  http.put(`/tickets/${id}/assign`, { workerId, remark })

export const confirmTicket = (id: number | string) => http.put(`/tickets/${id}/confirm`)

export const cancelTicket = (id: number | string, reason: string) =>
  http.put(`/tickets/${id}/cancel`, { reason })

export const urgeTicket = (id: number | string) => http.post(`/tickets/${id}/urge`)

export const addRemark = (id: number | string, content: string) =>
  http.post(`/tickets/${id}/remarks`, { content })

export const listWorkers = () => http.get('/users/workers')
