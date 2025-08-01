# 从 settings.gradle.kts 获取项目名（可选
$projectName = "Neo-lang"  # 你可以手动设置，或从文件读取

# 设置代码页为 UTF-8 (65001)
chcp 65001 > $null

# 1. 清理并构建项目
Write-Host "1/3 正在清理并构建项目..." -ForegroundColor Green
.\gradlew.bat clean jar --quiet

# 2. 检查 build/libs 下是否有 jar 文件
$jarFile = "build/libs/$projectName.jar"
if (-Not (Test-Path $jarFile)) {
    Write-Host "❌ 找不到 jar 文件: $jarFile" -ForegroundColor Red
    exit 1
}

# 3. 运行 jar 文件
Write-Host "2/3 正在运行程序..." -ForegroundColor Green
Write-Host "--------------------------------------------------" -ForegroundColor Yellow
Write-Host "现在你可以输入内容了（输入 exit 退出）：" -ForegroundColor Cyan
Write-Host "--------------------------------------------------" -ForegroundColor Yellow

java "-Dfile.encoding=UTF-8" -jar $jarFile

# 4. 结束
Write-Host "3/3 程序已退出" -ForegroundColor Green