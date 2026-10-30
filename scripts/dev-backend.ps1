# 本地启动后端：加载 .env 后运行 Spring Boot
# 用法：.\scripts\dev-backend.ps1         启动服务
#       .\scripts\dev-backend.ps1 -Check  只检查工具链与环境变量，不启动
param(
    [switch]$Check
)

$ErrorActionPreference = 'Stop'
$root = Split-Path -Parent $PSScriptRoot
$backend = Join-Path $root 'backend'

# ---- 1. 加载 .env ----
. (Join-Path $PSScriptRoot 'load-env.ps1')

# ---- 2. 定位 Maven：mvnw > 项目内 .tools > PATH ----
$mvn = $null
if (Test-Path (Join-Path $backend 'mvnw.cmd')) {
    $mvn = Join-Path $backend 'mvnw.cmd'
} elseif (Test-Path (Join-Path $root '.tools\apache-maven-3.9.9\bin\mvn.cmd')) {
    $mvn = Join-Path $root '.tools\apache-maven-3.9.9\bin\mvn.cmd'
} elseif (Get-Command mvn -ErrorAction SilentlyContinue) {
    $mvn = 'mvn'
} else {
    throw '未找到 Maven：请放置 .tools\apache-maven-3.9.9，或安装 mvn 到 PATH'
}

# ---- 3. 定位 JDK：JAVA_HOME > 项目内 JBR > IDE 自带 JBR ----
if (-not $env:JAVA_HOME) {
    $candidates = @(
        (Join-Path $root '.tools\jbr'),
        'E:\pytest\PyCharm Community Edition 2025.1.3.1\jbr',
        'E:\DEVECO-STUDIO\DevEco Studio\jbr'
    )
    foreach ($candidate in $candidates) {
        if (Test-Path (Join-Path $candidate 'bin\java.exe')) {
            $env:JAVA_HOME = $candidate
            break
        }
    }
}

# ---- 4. 本地依赖仓库放项目内，避免写入用户目录 ----
if (-not $env:MAVEN_OPTS -or $env:MAVEN_OPTS -notmatch 'maven.repo.local') {
    $env:MAVEN_OPTS = "-Dmaven.repo.local=$(Join-Path $root '.tools\m2repo') -Dfile.encoding=UTF-8"
}

if (-not $env:UPLOAD_DIR) {
    $env:UPLOAD_DIR = Join-Path $root 'data\uploads'
}

Write-Host "Maven     : $mvn"
Write-Host "JAVA_HOME : $env:JAVA_HOME"
Write-Host "数据库    : $($env:DB_HOST):$($env:DB_PORT)/$($env:DB_NAME) 用户=$($env:DB_USERNAME)"
Write-Host "服务端口  : $($env:SERVER_PORT)"

if ($Check) {
    if (-not $env:JAVA_HOME) {
        throw '未找到 JDK：请安装 JDK 21 或设置 JAVA_HOME'
    }
    Write-Host '检查通过，未启动服务。' -ForegroundColor Green
    return
}

Write-Host '正在启动 Spring Boot，首次启动需等待编译...' -ForegroundColor Cyan
Push-Location $backend
try {
    & $mvn spring-boot:run
} finally {
    Pop-Location
}
