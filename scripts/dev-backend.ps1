# 本地启动后端：加载 .env 后运行 Spring Boot
# 用法：.\scripts\dev-backend.ps1
$ErrorActionPreference = 'Stop'

. (Join-Path $PSScriptRoot 'load-env.ps1')

$backend = Join-Path (Split-Path -Parent $PSScriptRoot) 'backend'
Push-Location $backend
try {
    if (Test-Path '.\mvnw.cmd') {
        & .\mvnw.cmd spring-boot:run
    } elseif (Get-Command mvn -ErrorAction SilentlyContinue) {
        & mvn spring-boot:run
    } else {
        throw '未找到 Maven：既没有 mvnw，也没有 mvn 命令'
    }
} finally {
    Pop-Location
}
