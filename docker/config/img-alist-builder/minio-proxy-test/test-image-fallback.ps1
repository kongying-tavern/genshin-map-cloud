#!/usr/bin/env pwsh

# ============================================
# MinIO Proxy 图片回退测试脚本
# ============================================

# 参数定义
param(
    [Parameter(Mandatory = $true)]
    [string]$BaseUrl,

    [Parameter(Mandatory = $false)]
    [string]$CsvPath
)

# 如果没有指定 CSV 路径，使用默认值
if ( [string]::IsNullOrEmpty($CsvPath))
{
    $CsvPath = Join-Path $PSScriptRoot "test-image-fallback-cases.csv"
}

# 函数：根据二进制头判断图片格式
function Get-ImageFormat
{
    param(
        [byte[]]$Bytes
    )

    if ($null -eq $Bytes -or $Bytes.Length -lt 12)
    {
        return "UNKNOWN"
    }

    # 转换为十六进制字符串
    $hexString = [System.BitConverter]::ToString($Bytes[0..15]).Replace("-", "")

    # 检查 WebP (RIFF....WEBP)
    # 字节 0-3: RIFF (52494646), 字节 8-11: WEBP (57454250)
    # 注意：Substring 的参数是字符位置，每字节=2 字符
    if ($hexString.Substring(0, 8) -eq "52494646" -and $hexString.Substring(16, 8) -eq "57454250")
    {
        return "WEBP"
    }

    # 检查 PNG (\x89PNG)
    if ($hexString.Substring(0, 8) -eq "89504E47")
    {
        return "PNG"
    }

    # 检查 JPEG (FFD8FF)
    if ($hexString.Substring(0, 6) -eq "FFD8FF")
    {
        return "JPG"
    }

    # 检查 GIF (GIF8)
    if ($hexString.Substring(0, 8) -eq "47494638")
    {
        return "GIF"
    }

    return "UNKNOWN"
}

# 函数：从文件名解析各部分信息
function Parse-ImagePath
{
    param(
        [string]$ImagePath
    )

    # 格式：ORIG_TYPE~FALLBACK_TYPE~WEBP_SUPPORT.EXT
    $imagePathBase = [System.IO.Path]::GetFileNameWithoutExtension($ImagePath)
    $pathParts = $imagePathBase.Split('~')

    # 提取 Original_Ext（ORIG_TYPE 部分，即第一个~前的内容）
    $originalExt = if ($pathParts.Count -ge 1)
    {
        $pathParts[0].ToUpper()
    }
    else
    {
        ""
    }

    # 提取 Fallback_Ext（FALLBACK_TYPE 部分，即第二个部分）
    $fallbackExt = ""
    if ($pathParts.Count -ge 2)
    {
        $fbPart = $pathParts[1]

        # 如果是 no_fb，表示没有 fallback，为空
        # 如果是 X_fb 格式，提取 X
        if ($fbPart -ne 'no_fb')
        {
            if ($fbPart -match '^(.*)_fb$')
            {
                $fallbackExt = $matches[1].ToUpper()
            }
            else
            {
                # 其他情况直接使用原值
                $fallbackExt = $fbPart.ToUpper()
            }
        }
    }

    # 提取 Has_WebP_File（WEBP_SUPPORT 部分，即第三个部分）
    $hasWebPFile = ""
    if ($pathParts.Count -ge 3)
    {
        $webpPart = $pathParts[2]
        if ($webpPart -eq 'with_webp')
        {
            $hasWebPFile = "✅ 有"
        }
        elseif ($webpPart -eq 'no_webp')
        {
            $hasWebPFile = "❌ 无"
        }
    }

    return @{
        OriginalExt = $originalExt
        FallbackExt = $fallbackExt
        HasWebPFile = $hasWebPFile
    }
}

# 函数：格式化状态显示
function Format-Status
{
    param(
        [bool]$Success
    )

    if ($Success)
    {
        return "✅ PASS"
    }
    else
    {
        return "❌ FAIL"
    }
}

# 函数：格式化 WebP Header 显示
function Format-WebPHeader
{
    param(
        [string]$UseWebpHeader
    )

    if ($UseWebpHeader -eq "1")
    {
        return "✅ 是"
    }
    else
    {
        return "❌ 否"
    }
}

# 颜色定义
$Green = "`e[32m"
$Red = "`e[31m"
$Yellow = "`e[33m"
$Reset = "`e[0m"

Write-Host "`n${Yellow}========================================${Reset}"
Write-Host "${Yellow}MinIO Proxy 图片回退测试${Reset}"
Write-Host "${Yellow}========================================${Reset}"
Write-Host "${Cyan}Base URL: ${Reset}$BaseUrl"
Write-Host "${Cyan}CSV Path: ${Reset}$CsvPath`n"

$testCases = Import-Csv -Path $CsvPath

$totalTests = 0
$passedTests = 0
$failedTests = 0
$failedTestsDetail = @()
$results = @()  # 存储所有测试结果用于表格展示

# 分组统计
$webpSupportTests = @{ Total = 0; Passed = 0; Failed = 0 }
$noWebpSupportTests = @{ Total = 0; Passed = 0; Failed = 0 }

foreach ($testCase in $testCases)
{
    $totalTests++
    $imagePath = $testCase.image_path
    $expStatus = $testCase.exp_status
    $expType = $testCase.exp_type
    $useWebpHeader = $testCase.use_webp_header
    $fallbackRoutine = $testCase.fallback_routine

    # 直接使用 CSV 中的路径，与 BaseUrl 拼接
    $url = "$BaseUrl/$imagePath"

    try
    {
        # 根据 use_webp_header 决定是否发送 WebP 支持头
        if ($useWebpHeader -eq "1")
        {
            $headers = @{
                "Accept" = "image/webp,image/apng,image/*,*/*;q=0.8"
            }
        }
        else
        {
            $headers = @{ }
        }

        $response = Invoke-WebRequest -Uri $url -Headers $headers -Method GET -UseBasicParsing -TimeoutSec 10

        # 检查状态码
        if ($expStatus -eq "404")
        {
            $failedTests++
            $failedTestsDetail += [PSCustomObject]@{
                TestNumber = $totalTests
                ImagePath = $imagePath
                FallbackRoutine = $fallbackRoutine
                Error = "Expected 404 but got $( $response.StatusCode )"
            }
        }
        else
        {
            # 获取响应内容（原始字节）- 直接使用 Content 属性
            $contentBytes = $response.Content

            # 根据二进制头判断实际格式
            $actualFormat = Get-ImageFormat -Bytes $contentBytes

            $expectedTypeLower = $expType.ToLower()

            # 验证格式是否匹配预期
            $formatMatch = switch ($expectedTypeLower)
            {
                "webp" {
                    $actualFormat -eq "WEBP"
                }
                "png" {
                    $actualFormat -eq "PNG"
                }
                "jpg" {
                    $actualFormat -eq "JPG"
                }
                "jfif" {
                    $actualFormat -eq "JPG"
                }  # JFIF 也是 JPEG
                default {
                    $false
                }
            }

            if ($formatMatch)
            {
                $passedTests++
            }
            else
            {
                $failedTests++
                $failedTestsDetail += [PSCustomObject]@{
                    TestNumber = $totalTests
                    ImagePath = $imagePath
                    FallbackRoutine = $fallbackRoutine
                    Error = "Expected $expType but got $actualFormat"
                }
            }

            # 记录测试结果
            $results += [PSCustomObject]@{
                Success = $formatMatch
                URL = $url
                ImagePath = $imagePath
                UseWebpHeader = $useWebpHeader
                FallbackRoutine = $fallbackRoutine
                ExpectedStatus = $expStatus
                ExpectedType = $expType
                ActualStatus = $response.StatusCode
                ActualType = $actualFormat
            }
        }
    }
    catch
    {
        if ($expStatus -eq "404" -and $_.Exception.Response.StatusCode -eq 404)
        {
            $passedTests++
            # 记录测试结果（404 情况）
            $results += [PSCustomObject]@{
                Success = $true
                URL = $url
                ImagePath = $imagePath
                UseWebpHeader = $useWebpHeader
                FallbackRoutine = $fallbackRoutine
                ExpectedStatus = $expStatus
                ExpectedType = $expType
                ActualStatus = 404
                ActualType = "N/A"
            }
        }
        else
        {
            $failedTests++
            $failedTestsDetail += [PSCustomObject]@{
                TestNumber = $totalTests
                ImagePath = $imagePath
                FallbackRoutine = $fallbackRoutine
                Error = $_.Exception.Message
            }
            # 记录测试结果（错误情况）
            $actualStatus = if ($_.Exception.Response)
            {
                $_.Exception.Response.StatusCode
            }
            else
            {
                "ERROR"
            }
            $results += [PSCustomObject]@{
                Success = $false
                URL = $url
                ImagePath = $imagePath
                UseWebpHeader = $useWebpHeader
                FallbackRoutine = $fallbackRoutine
                ExpectedStatus = $expStatus
                ExpectedType = $expType
                ActualStatus = $actualStatus
                ActualType = "ERROR"
            }
        }
    }

    # 更新分组统计
    if ($useWebpHeader -eq "1")
    {
        $webpSupportTests.Total++
        if ($failedTests -eq ($webpSupportTests.Failed + $webpSupportTests.Passed))
        {
            $webpSupportTests.Passed++
        }
        else
        {
            $webpSupportTests.Failed++
        }
    }
    else
    {
        $noWebpSupportTests.Total++
        if ($failedTests -eq ($noWebpSupportTests.Failed + $noWebpSupportTests.Passed))
        {
            $noWebpSupportTests.Passed++
        }
        else
        {
            $noWebpSupportTests.Failed++
        }
    }
}

# 输出统计
Write-Host "`n${Yellow}========================================${Reset}"
Write-Host "${Yellow}测试结果详情${Reset}"
Write-Host "${Yellow}========================================${Reset}`n"

# 创建格式化表格
$tableResults = $results | ForEach-Object {
    # 规范化 ActualStatus
    $normalizedActualStatus = if ($_.ActualStatus -eq "NotFound")
    {
        404
    }
    elseif ($_.ActualStatus -is [int])
    {
        $_.ActualStatus
    }
    else
    {
        $_.ActualStatus
    }

    # 规范化 ActualType（ERROR -> 空字符串）
    $normalizedActualType = if ($_.ActualType -eq "ERROR" -or $_.ActualType -eq "N/A")
    {
        ""
    }
    else
    {
        $_.ActualType
    }

    # 提取 URL 扩展名
    $urlExtension = [System.IO.Path]::GetExtension($_.URL).TrimStart('.').ToUpper()

    # 从 image_path 解析各部分信息
    $parsedInfo = Parse-ImagePath -ImagePath $_.ImagePath

    # 格式化状态显示
    $status = Format-Status -Success $_.Success

    # 格式化 WebP Header 显示
    $testWithWebP = Format-WebPHeader -UseWebpHeader $_.UseWebpHeader

    [PSCustomObject]@{
        Status = $status
        URL = $_.URL
        URL_Ext = $urlExtension
        Original_Ext = $parsedInfo.OriginalExt
        Fallback_Ext = $parsedInfo.FallbackExt
        Has_WebP_File = $parsedInfo.HasWebPFile
        Test_With_WebP_Header = $testWithWebP
        ExpectedStatus = $_.ExpectedStatus
        ExpectedType = $_.ExpectedType
        ActualStatus = $normalizedActualStatus
        ActualType = $normalizedActualType
        FallbackRoutine = $_.FallbackRoutine
    }
}

# 使用 Format-Table 输出控制台表格
$tableResults | Format-Table -AutoSize -Wrap

# 同时显示 GUI 表格（如果支持）
try
{
    $tableResults | Out-GridView -Title "MinIO Proxy 图片回退测试结果"
}
catch
{
    # Out-GridView 在某些环境下不可用（如 PowerShell Core、远程会话等）
    Write-Host "`n${Yellow}提示：${Reset}GUI 表格不可用，已显示控制台表格。"
}

# 根据失败情况决定退出码
if ($failedTests -gt 0)
{
    exit 1
}
else
{
    Write-Host "${Green}✅ All tests passed!${Reset}`n"
}
