# 启动移动端开发服务器
# 用法：.\scripts\dev-mobile.ps1
$ErrorActionPreference = 'Stop'

$web = Join-Path (Split-Path -Parent $PSScriptRoot) 'mobile-web'
Push-Location $web
try {
    if (-not (Test-Path 'node_modules')) {
        Write-Host '首次运行，安装依赖...'
        & npm install --no-fund --no-audit
    }
    & npm run dev
} finally {
    Pop-Location
}
