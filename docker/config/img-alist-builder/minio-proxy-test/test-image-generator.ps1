# 测试图片生成脚本 - 生成所有格式的测试图片
# 包括：JFIF, JPEG, JPG, PNG 的 with_webp 和 no_webp 版本

param(
    [Parameter(Mandatory = $false)]
    [int]$Width = 20,

    [Parameter(Mandatory = $false)]
    [int]$Height = 20
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

# 验证长宽是否为偶数
if ($Width % 2 -ne 0)
{
    Write-Host "❌ 错误：宽度 ($Width) 必须是偶数" -ForegroundColor Red
    exit 1
}

if ($Height % 2 -ne 0)
{
    Write-Host "❌ 错误：高度 ($Height) 必须是偶数" -ForegroundColor Red
    exit 1
}

# 输出目录
$outputDir = Join-Path $PSScriptRoot "images"
if (!(Test-Path $outputDir))
{
    New-Item -ItemType Directory -Path $outputDir | Out-Null
}

Write-Host "`n正在生成测试图片..." -ForegroundColor Cyan
Write-Host "输出目录：$outputDir"
Write-Host "图片尺寸：${Width}x${Height}`n"

# 使用配置文件中的颜色定义（已在 config 中定义，此处不需要重复）

# 列表 1: 测试用例扩展名 (从配置文件加载)
$testcaseExtensions = $ImageFormatConfigs

# 列表 2: WebP 存在性 (从配置文件加载)
$webpExistence = $WebPExistence

# 过滤需要生成图片的格式
$imageFormats = $ImageFormatConfigs | Where-Object { $_.GenerateImage }

# 生成所有文件配置
$fileConfigs = @()
foreach ($formatConfig in $imageFormats)
{
    foreach ($webp in $WebPExistence)
    {
        # 1. 生成 no_fb 类型 (只生成当前类型)
        $fileConfigs += @{
            Name = "$( $formatConfig.Ext )~no_fb~$( $webp.Suffix ).$( $formatConfig.Ext )"
            Ext = $formatConfig.Ext
            CornerColor = $webp.CornerColor
            EdgeColor = $formatConfig.EdgeColor
            ImageFormat = $formatConfig.ImageFormat
        }

        # 2. 生成 fallback 类型 (回退链中排除当前类型 X 后的每个类型)
        $fallbackExts = $formatConfig.FallbackChain | Where-Object { $_ -ne $formatConfig.Ext }

        if ($fallbackExts.Count -eq 0)
        {
            continue
        }

        foreach ($fallbackExt in $fallbackExts)
        {
            $fbConfig = $ImageFormatConfigs | Where-Object { $_.Ext -eq $fallbackExt }

            # 当前类型的文件
            $fileConfigs += @{
                Name = "$( $formatConfig.Ext )~$( $fallbackExt )_fb~$( $webp.Suffix ).$( $formatConfig.Ext )"
                Ext = $formatConfig.Ext
                CornerColor = $webp.CornerColor
                EdgeColor = $formatConfig.EdgeColor
                ImageFormat = $formatConfig.ImageFormat
            }

            # fallback 类型的文件
            $fileConfigs += @{
                Name = "$( $formatConfig.Ext )~$( $fallbackExt )_fb~$( $webp.Suffix ).$( $fallbackExt )"
                Ext = $fallbackExt
                CornerColor = $webp.CornerColor
                EdgeColor = $formatConfig.EdgeColor
                ImageFormat = $fbConfig.ImageFormat
            }
        }
    }
}

# 使用配置文件中的颜色定义和程序集（已在 config 中定义，此处不需要重复）

foreach ($config in $fileConfigs)
{
    $fileName = $config.Name
    $filePath = Join-Path $outputDir $fileName

    # 创建位图
    $bitmap = New-Object System.Drawing.Bitmap($Width, $Height)

    # 填充像素 - 分为四个区域
    for ($y = 0; $y -lt $Height; $y++) {
        for ($x = 0; $x -lt $Width; $x++) {
            if (($x -lt $Width / 2 -and $y -lt $Height / 2) -or ($x -ge $Width / 2 -and $y -ge $Height / 2))
            {
                $bitmap.SetPixel($x, $y, $config.CornerColor)
            }
            else
            {
                $bitmap.SetPixel($x, $y, $config.EdgeColor)
            }
        }
    }

    # 保存图片
    try
    {
        $bitmap.Save($filePath, $config.ImageFormat)
        Write-Host "✅ $fileName" -ForegroundColor Green
    }
    catch
    {
        Write-Host "❌ $fileName 失败：$_" -ForegroundColor Red
    }
    finally
    {
        $bitmap.Dispose()
    }
}

Write-Host "`n✅ 所有测试图片生成完成!`n" -ForegroundColor Green
