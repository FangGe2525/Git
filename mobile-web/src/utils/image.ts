/**
 * 上传前压缩图片：最长边限制 + JPEG 质量。
 * 后端只接受 5MB 以内的图片，手机原图往往超过这个体积。
 */
export async function compressImage(file: File, maxEdge = 1920, quality = 0.8): Promise<File> {
  if (!file.type.startsWith('image/')) {
    return file
  }
  try {
    const bitmap = await createImageBitmap(file)
    const scale = Math.min(1, maxEdge / Math.max(bitmap.width, bitmap.height))
    if (scale === 1 && file.size <= 1024 * 1024) {
      return file
    }
    const canvas = document.createElement('canvas')
    canvas.width = Math.round(bitmap.width * scale)
    canvas.height = Math.round(bitmap.height * scale)
    const ctx = canvas.getContext('2d')
    if (!ctx) return file
    ctx.drawImage(bitmap, 0, 0, canvas.width, canvas.height)
    const blob = await new Promise<Blob | null>((resolve) =>
      canvas.toBlob((b) => resolve(b), 'image/jpeg', quality)
    )
    if (!blob) return file
    const name = file.name.replace(/\.[^.]+$/, '') + '.jpg'
    return new File([blob], name, { type: 'image/jpeg' })
  } catch {
    // 压缩失败不阻塞上传，交给后端做大小校验
    return file
  }
}
