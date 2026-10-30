export const STATUS_LABEL: Record<string, string> = {
  PENDING: '待接单',
  ACCEPTED: '已接单',
  REPAIRING: '处理中',
  VERIFYING: '待确认',
  COMPLETED: '已完成',
  CANCELED: '已作废'
}

export const STATUS_TYPE: Record<string, 'primary' | 'success' | 'warning' | 'danger' | 'default'> = {
  PENDING: 'warning',
  ACCEPTED: 'primary',
  REPAIRING: 'primary',
  VERIFYING: 'warning',
  COMPLETED: 'success',
  CANCELED: 'default'
}

export const PRIORITY_LABEL: Record<string, string> = {
  LOW: '低',
  MEDIUM: '中',
  HIGH: '高'
}

export const PRIORITY_TYPE: Record<string, 'primary' | 'success' | 'warning' | 'danger' | 'default'> = {
  LOW: 'default',
  MEDIUM: 'primary',
  HIGH: 'danger'
}

export const SOURCE_LABEL: Record<string, string> = {
  SCAN: '扫码报修',
  PHONE: '电话代报',
  WALK_IN: '上门报修',
  OTHER: '其他'
}

/** 常见工种，先用固定列表，后续接配置表 */
export const CATEGORIES = ['水电', '电工', '木工', '瓦工', '电梯', '保洁', '安防', '其他']

export const PROBLEM_TYPES = ['漏水', '断电', '堵塞', '门窗损坏', '电梯故障', '照明', '其他']

export function formatTime(value?: string) {
  if (!value) return ''
  return value.replace('T', ' ').slice(0, 16)
}
