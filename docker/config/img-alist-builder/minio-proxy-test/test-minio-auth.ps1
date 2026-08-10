#!/usr/bin/env pwsh

# ============================================
# MinIO MC 鉴权测试脚本
# ============================================

param(
    [Parameter(Mandatory = $true)]
    [string]$AccessKey,

    [Parameter(Mandatory = $true)]
    [string]$SecretKey,

    [Parameter(Mandatory = $true)]
    [int]$MinioPort,

    [Parameter(Mandatory = $false)]
    [string]$AliasName = "test-minio"
)

# 颜色定义
$Green = "`e[32m"
$Red = "`e[31m"
$Yellow = "`e[33m"
$Cyan = "`e[36m"
$Reset = "`e[0m"

# 构建容器内访问地址
$internalMinioUrl = "http://host.docker.internal:$MinioPort"

Write-Host "`n${Yellow}========================================${Reset}"
Write-Host "${Yellow}MinIO MC 鉴权测试${Reset}"
Write-Host "${Yellow}========================================${Reset}"
Write-Host "${Cyan}MinIO Port: ${Reset}$MinioPort"
Write-Host "${Cyan}Internal URL: ${Reset}$internalMinioUrl"
Write-Host "${Cyan}Access Key: ${Reset}$AccessKey"
Write-Host "${Cyan}Secret Key: ${Reset}***"
Write-Host "${Cyan}Alias Name: ${Reset}$AliasName`n"

# 检查 Docker 是否可用
try
{
    docker --version | Out-Null
    Write-Host "✅ Docker 已安装" -ForegroundColor Green
}
catch
{
    Write-Host "❌ Docker 未安装或不可用" -ForegroundColor Red
    exit 1
}

# 停止并清理旧的容器
Write-Host "`n正在清理旧的 MinIO MC 容器..." -ForegroundColor Cyan
docker rm -f minio-proxy-mc 2> $null | Out-Null

# 启动 MinIO MC 容器
Write-Host "正在启动 MinIO MC 容器..." -ForegroundColor Cyan
try
{
    docker run -d --name minio-proxy-mc --rm `
        --entrypoint /bin/sh `
        minio/mc:latest `
        -c "while true; do sleep 1000; done" | Out-Null

    Start-Sleep -Seconds 2

    # 检查容器是否启动成功
    $containerStatus = docker inspect -f '{{.State.Status}}' minio-proxy-mc 2> $null

    if ($containerStatus -eq "running")
    {
        Write-Host "✅ MinIO MC 容器启动成功" -ForegroundColor Green
    }
    else
    {
        throw "容器状态异常：$containerStatus"
    }
}
catch
{
    Write-Host "❌ MinIO MC 容器启动失败：$_" -ForegroundColor Red
    exit 1
}

# 配置 MC 别名并测试鉴权
Write-Host "`n正在配置 MC 别名并测试鉴权..." -ForegroundColor Cyan

$mcCommand = "mc alias set $AliasName $internalMinioUrl $AccessKey $SecretKey"
Write-Host "执行命令：$mcCommand" -ForegroundColor Gray

try
{
    $result = docker exec minio-proxy-mc sh -c $mcCommand 2>&1

    if ($LASTEXITCODE -eq 0)
    {
        Write-Host "`n✅ 鉴权成功!" -ForegroundColor Green
        Write-Host "`nMC 返回信息:" -ForegroundColor Cyan
        Write-Host $result -ForegroundColor Gray

        # 验证别名是否可用
        Write-Host "`n正在验证别名可用性..." -ForegroundColor Cyan
        $verifyCommand = "mc ls $AliasName"
        $verifyResult = docker exec minio-proxy-mc sh -c $verifyCommand 2>&1

        if ($LASTEXITCODE -eq 0)
        {
            Write-Host "✅ 别名验证成功，可以正常访问 MinIO" -ForegroundColor Green
            if ($verifyResult)
            {
                Write-Host "`n存储桶列表:" -ForegroundColor Cyan
                Write-Host $verifyResult -ForegroundColor Gray
            }
            else
            {
                Write-Host "`n⚠️  无存储桶 (空)" -ForegroundColor Yellow
            }
        }
        else
        {
            Write-Host "⚠️  别名验证失败：$verifyResult" -ForegroundColor Yellow
        }

        $testResult = [PSCustomObject]@{
            Status = "SUCCESS"
            AliasName = $AliasName
            MinioUrl = $MinioUrl
            Message = "鉴权成功，可以正常访问"
        }

    }
    else
    {
        Write-Host "`n❌ 鉴权失败!" -ForegroundColor Red
        Write-Host "`n错误信息:" -ForegroundColor Cyan
        Write-Host $result -ForegroundColor Gray

        # 分析错误类型
        if ($result -match "403" -or $result -match "Forbidden")
        {
            Write-Host "`n⚠️  错误类型：访问被拒绝 (403 Forbidden)" -ForegroundColor Yellow
            Write-Host "可能原因：AccessKey 或 SecretKey 错误" -ForegroundColor Yellow
        }
        elseif ($result -match "401" -or $result -match "Unauthorized")
        {
            Write-Host "`n⚠️  错误类型：未授权 (401 Unauthorized)" -ForegroundColor Yellow
            Write-Host "可能原因：缺少认证信息或认证格式错误" -ForegroundColor Yellow
        }
        elseif ($result -match "connection refused" -or $result -match "timeout")
        {
            Write-Host "`n⚠️  错误类型：连接失败" -ForegroundColor Yellow
            Write-Host "可能原因：MinIO 服务不可达或网络问题" -ForegroundColor Yellow
        }
        else
        {
            Write-Host "`n⚠️  错误类型：未知错误" -ForegroundColor Yellow
        }

        $testResult = [PSCustomObject]@{
            Status = "FAILED"
            AliasName = $AliasName
            MinioUrl = $MinioUrl
            Message = "鉴权失败：$result"
        }
    }

}
catch
{
    Write-Host "`n❌ 执行出错：$_" -ForegroundColor Red
    $testResult = [PSCustomObject]@{
        Status = "ERROR"
        AliasName = $AliasName
        MinioUrl = $MinioUrl
        Message = $_.Exception.Message
    }
}

# 清理容器
Write-Host "`n正在清理容器..." -ForegroundColor Cyan
docker rm -f minio-proxy-mc 2> $null | Out-Null
Write-Host "✅ 容器已清理" -ForegroundColor Green

# 输出测试结果
Write-Host "`n${Yellow}========================================${Reset}"
Write-Host "${Yellow}测试结果${Reset}"
Write-Host "${Yellow}========================================${Reset}"
$testResult | Format-List

# 根据结果退出
if ($testResult.Status -eq "SUCCESS")
{
    exit 0
}
else
{
    exit 1
}
