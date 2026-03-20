#!/usr/bin/env pwsh

# ============================================
# 测试用例生成脚本
# 用于生成 MinIO Proxy 图片回退测试的 CSV 用例
# ============================================

param(
    [Parameter(Mandatory = $false)]
    [string]$OutputPath = (Join-Path $PSScriptRoot "test-image-fallback-cases.csv")
)

# 加载配置文件
$configPath = Join-Path $PSScriptRoot "test-image-config.ps1"
if (Test-Path $configPath)
{
    . $configPath
}
else
{
    Write-Host "❌ 找不到配置文件：$configPath" -ForegroundColor Red
    exit 1
}

Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "测试用例生成脚本" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "输出路径：$OutputPath`n"

# 配置数据
$webpSupportOptions = $WebPExistence | ForEach-Object { $_.Suffix }
$requestExtensions = $ImageFormatConfigs | Where-Object { $_.IsTestRequestExt } | ForEach-Object { $_.Ext }

# 函数：判断文件是否存在
# 基于基础数据判断，而非从文件名解析
# 基础数据：origType, fallbackType, webpSupport
# 判断规则：
# 1. 如果 webpSupport = 'with_webp'，则 webp 文件存在
# 2. 如果 fallbackType ≠ 'no_fb'，则 fallback 类型对应的文件存在
# 3. origType 对应的文件总是存在
function Test-FileExists
{
    param(
        [string]$OrigType,
        [string]$FallbackType,
        [string]$WebpExistence,
        [string[]]$FallbackChain,
        [string]$FileType
    )

    if ($WebpExistence -eq 'with_webp' -and $FileType -eq 'webp')
    {
        return $true
    }

    if ($FallbackType -ne 'no_fb')
    {
        $fallbackExt = $FallbackType -replace '_fb', ''
        if ($FileType -eq $fallbackExt)
        {
            return $true
        }
    }

    if ($FileType -eq $OrigType)
    {
        return $true
    }

    return $false
}

# 函数：查找第一个存在的文件类型
function Find-FirstExistingFile
{
    param(
        [string]$OrigType,
        [string]$FallbackType,
        [string]$WebpExistence,
        [string[]]$FallbackChain,
        [string[]]$RoutineChain
    )

    foreach ($fileType in $RoutineChain)
    {
        $exists = Test-FileExists -OrigType $OrigType -FallbackType $FallbackType -WebpExistence $WebpExistence -FallbackChain $FallbackChain -FileType $fileType
        if ($exists)
        {
            return $fileType
        }
    }

    return $null
}

# 函数：根据找到的第一个文件类型确定预期状态和类型
function Get-ExpectedResult
{
    param(
        [string]$FirstExistingFile
    )

    if ($FirstExistingFile)
    {
        $expectedStatus = '200'
        $config = $ImageFormatConfigs | Where-Object { $_.Ext -eq $FirstExistingFile }
        $expectedType = $config.ResponseBinaryFormat
    }
    else
    {
        $expectedStatus = '404'
        $expectedType = ''
    }

    return @{
        Status = $expectedStatus
        Type = $expectedType
    }
}

# 函数：生成单个测试用例
function New-TestCase
{
    param(
        [string]$TestFormat,
        [string]$OrigType,
        [string]$FallbackType,
        [string[]]$FallbackChain,
        [string]$WebpExistence,
        [string]$RequestExt,
        [int]$UseWebpHeader,
        [string[]]$RequestExtFallbackChain
    )

    $fileName = "${OrigType}~${FallbackType}~${WebpExistence}.${RequestExt}"

    # 访问 .webp 时，只检查 webp 文件是否存在
    if ($RequestExt -eq 'webp')
    {
        if ($WebpExistence -eq 'with_webp')
        {
            $firstExistingFile = 'webp'
        }
        else
        {
            $firstExistingFile = $null
        }
        $fallbackRoutine = '-'
    }
    else
    {
        if ($UseWebpHeader -eq 1)
        {
            $routineChain = @('WEBP') + $RequestExtFallbackChain
        }
        else
        {
            $routineChain = $RequestExtFallbackChain
        }
        $fallbackRoutine = if ($routineChain)
        {
            $routineChain | ForEach-Object { $_.ToUpper() } | Join-String -Separator ' - '
        }
        else
        {
            '-'
        }

        $firstExistingFile = Find-FirstExistingFile -OrigType $OrigType -FallbackType $FallbackType -WebpExistence $WebpExistence -FallbackChain $FallbackChain -RoutineChain $routineChain
    }

    $result = Get-ExpectedResult -FirstExistingFile $firstExistingFile

    return [PSCustomObject]@{
        test_format = $TestFormat
        image_path = $fileName
        exp_status = $result.Status
        exp_type = $result.Type
        use_webp_header = $UseWebpHeader
        fallback_routine = $fallbackRoutine
    }
}

# 过滤需要生成测试用例的格式
$testFormats = $ImageFormatConfigs | Where-Object { $_.GenerateTestCases }

# 生成所有测试用例
$testCases = @()

foreach ($headerOption in $UseWebpHeaderOptions)
{
    $useWebpHeader = $headerOption.Value

    foreach ($formatConfig in $testFormats)
    {
        $origType = $formatConfig.Ext
        $fallbackChain = $formatConfig.FallbackChain

        $fallbackTypesList = @('no_fb')
        $fallbackExts = $fallbackChain | Where-Object { $_ -ne $origType }
        foreach ($fbExt in $fallbackExts)
        {
            $fallbackTypesList += "${fbExt}_fb"
        }

        foreach ($fallbackType in $fallbackTypesList)
        {
            foreach ($webpSupport in $webpSupportOptions)
            {
                foreach ($requestExt in $requestExtensions)
                {
                    $requestExtConfig = $ImageFormatConfigs | Where-Object { $_.Ext -eq $requestExt }
                    $requestExtFallbackChain = $requestExtConfig.FallbackChain

                    $testCase = New-TestCase `
                        -TestFormat $formatConfig.Ext.ToUpper() `
                        -OrigType $origType `
                        -FallbackType $fallbackType `
                        -FallbackChain $fallbackChain `
                        -WebpExistence $webpSupport `
                        -RequestExt $requestExt `
                        -UseWebpHeader $useWebpHeader `
                        -RequestExtFallbackChain $requestExtFallbackChain
                    $testCases += $testCase
                }
            }
        }
    }
}

# 导出到 CSV（不使用引号）
$testCases | Export-Csv -Path $OutputPath -NoTypeInformation -Encoding UTF8 -UseQuotes Never

Write-Host "✅ 测试用例生成完成！" -ForegroundColor Green
Write-Host "共生成 $( $testCases.Count ) 个测试用例`n" -ForegroundColor Green
