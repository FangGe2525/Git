# 本地启动后端：加载 .env、按需打包、直接运行 jar
# 用法：.\scripts\dev-backend.ps1            需要时自动编译再启动
#       .\scripts\dev-backend.ps1 -Rebuild   强制重新打包
#       .\scripts\dev-backend.ps1 -Check     只检查环境，不启动
param(
    [switch]$Check,
    [switch]$Rebuild
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

# ---- 3. 定位 JDK：JAVA_HOME > IDE 自带 JBR ----
if (-not $env:JAVA_HOME -or -not (Test-Path (Join-Path $env:JAVA_HOME 'bin\java.exe'))) {
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
if (-not $env:JAVA_HOME) {
    throw '未找到 JDK 21：请安装 JDK 或设置 JAVA_HOME'
}
$java = Join-Path $env:JAVA_HOME 'bin\java.exe'

# ---- 4. 本地依赖仓库放项目内，避免写入用户目录 ----
if (-not $env:MAVEN_OPTS -or $env:MAVEN_OPTS -notmatch 'maven.repo.local') {
    $env:MAVEN_OPTS = "-Dmaven.repo.local=$(Join-Path $root '.tools\m2repo') -Dfile.encoding=UTF-8"
}

if (-not $env:UPLOAD_DIR) {
    $env:UPLOAD_DIR = Join-Path $root 'data\uploads'
}

# ---- 5. 判断是否需要重新打包 ----
$jar = Join-Path $backend 'target\community-repair-0.1.0-SNAPSHOT.jar'
$needBuild = $Rebuild -or (-not (Test-Path $jar))
if (-not $needBuild) {
    $jarTime = (Get-Item $jar).LastWriteTime
    $newest = $null
    foreach ($path in @($backend)) {
        $item = Get-ChildItem $path -Recurse -File -ErrorAction SilentlyContinue |
            Where-Object { $_.Extension -in '.java', '.yml', '.xml', '.properties' } |
            Sort-Object LastWriteTime -Descending |
            Select-Object -First 1
        if ($item -and (-not $newest -or $item.LastWriteTime -gt $newest.LastWriteTime)) {
            $newest = $item
        }
    }
    if ($newest -and $newest.LastWriteTime -gt $jarTime) {
        Write-Host "源码有变更（$($newest.Name)），需要重新打包。" -ForegroundColor Yellow
        $needBuild = $true
    }
}

Write-Host "Maven     : $mvn"
Write-Host "JAVA_HOME : $env:JAVA_HOME"
Write-Host "数据库    : $($env:DB_HOST):$($env:DB_PORT)/$($env:DB_NAME) 用户=$($env:DB_USERNAME)"
Write-Host "服务端口  : $($env:SERVER_PORT)"

if ($Check) {
    Write-Host "是否需要打包: $needBuild"
    Write-Host '检查通过，未启动服务。' -ForegroundColor Green
    return
}

# ---- 6. 打包 ----
if ($needBuild) {
    Write-Host '正在编译打包，首次需要 1-2 分钟...' -ForegroundColor Cyan
    Push-Location $backend
    try {
        & $mvn -B -ntp -DskipTests package
        if ($LASTEXITCODE -ne 0) { throw "打包失败（exit $LASTEXITCODE）" }
    } finally {
        Pop-Location
    }
}

# ---- 7. 启动 ----
Write-Host '启动中... 看到 Started CommunityRepairApplication 即为就绪。' -ForegroundColor Cyan
Write-Host '按 Ctrl+C 停止。' -ForegroundColor DarkGray
& $java -jar $jar
