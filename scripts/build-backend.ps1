# 编译/打包后端。优先使用项目内 .tools 工具链，其次 mvnw，最后 PATH 中的 mvn
# 用法：.\scripts\build-backend.ps1 [-Package]
param(
    [switch]$Package
)

$ErrorActionPreference = 'Stop'
$root = Split-Path -Parent $PSScriptRoot
$backend = Join-Path $root 'backend'
$goal = if ($Package) { 'package' } else { 'compile' }

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

if (-not $env:JAVA_HOME) {
    $jbr = 'E:\pytest\PyCharm Community Edition 2025.1.3.1\jbr'
    if (Test-Path $jbr) {
        $env:JAVA_HOME = $jbr
        Write-Host "使用 JBR 作为 JDK：$jbr"
    }
}

if (-not $env:MAVEN_OPTS -or $env:MAVEN_OPTS -notmatch 'maven.repo.local') {
    $env:MAVEN_OPTS = "-Dmaven.repo.local=$(Join-Path $root '.tools\m2repo') -Dfile.encoding=UTF-8"
}

Push-Location $backend
try {
    & $mvn -B -ntp -DskipTests $goal
    if ($LASTEXITCODE -ne 0) { throw "Maven $goal 失败（exit $LASTEXITCODE）" }
} finally {
    Pop-Location
}
