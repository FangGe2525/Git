export type TagType = 'success' | 'info' | 'warning' | 'danger' | 'primary'

export const STATUS_LABEL: Record<string, string> = {
  PENDING: '待接单',
  ACCEPTED: '已接单',
  REPAIRING: '处理中',
  VERIFYING: '待确认',
  COMPLETED: '已完成',
  CANCELED: '已作废'
}

export const STATUS_TYPE: Record<string, TagType> = {
  PENDING: 'warning',
  ACCEPTED: 'primary',
  REPAIRING: 'primary',
  VERIFYING: 'warning',
  COMPLETED: 'success',
  CANCELED: 'info'
}

export const STATUS_OPTIONS = Object.entries(STATUS_LABEL).map(([value, label]) => ({ value, label }))

export const PRIORITY_LABEL: Record<string, string> = { LOW: '低', MEDIUM: '中', HIGH: '高' }

export const PRIORITY_TYPE: Record<string, TagType> = {
  LOW: 'info',
  MEDIUM: 'primary',
  HIGH: 'danger'
}

export const SOURCE_LABEL: Record<string, string> = {
  SCAN: '扫码报修',
  PHONE: '电话代报',
  WALK_IN: '上门报修',
  OTHER: '其他'
}

export const CATEGORIES = ['水电', '电工', '木工', '瓦工', '电梯', '保洁', '安防', '其他']

/** 后端 LocalDateTime 序列化为 ISO，这里统一成 yyyy-MM-dd HH:mm */
export function formatTime(value?: string) {
  if (!value) return '-'
  return value.replace('T', ' ').slice(0, 16)
}

export function locationText(t: any) {
  if (!t) return '-'
  return [t.building, t.unit && `${t.unit}单元`, t.room && `${t.room}室`].filter(Boolean).join(' ') || '-'
}
