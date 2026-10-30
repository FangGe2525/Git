# 读取项目根目录的 .env，注入当前会话的环境变量
# 用法：. .\scripts\load-env.ps1
$ErrorActionPreference = 'Stop'

$root = Split-Path -Parent $PSScriptRoot
$envFile = Join-Path $root '.env'

if (-not (Test-Path $envFile)) {
    Write-Warning "未找到 .env，请先从 .env.example 复制一份：Copy-Item .env.example .env"
    return
}

Get-Content $envFile -Encoding UTF8 | ForEach-Object {
    $line = $_.Trim()
    if ($line -eq '' -or $line.StartsWith('#')) { return }
    $idx = $line.IndexOf('=')
    if ($idx -lt 1) { return }
    $name = $line.Substring(0, $idx).Trim()
    $value = $line.Substring($idx + 1).Trim()
    if ($value -eq '') { return }
    Set-Item -Path "env:$name" -Value $value
}

Write-Host "已加载 .env（$((Get-Content $envFile | Where-Object { $_ -match '^\s*[^#].*=' }).Count) 项）"
