# ============================================
# 配置文件
# 为所有脚本提供统一的配置数据
# ============================================

# 加载 System.Drawing 程序集
try
{
    Add-Type -AssemblyName System.Drawing -ErrorAction Stop
}
catch
{
    Write-Host "❌ 无法加载 System.Drawing，请确保安装了 .NET Framework" -ForegroundColor Red
    exit 1
}

# 颜色定义 - 描述图片块的颜色配置
$ImageBlockColors = @{
    WithWebPColor = [System.Drawing.Color]::FromArgb(138, 43, 226)  # BlueViolet - WebP 存在标识
    NoWebPColor = [System.Drawing.Color]::FromArgb(64, 64, 64)    # 深灰色 - WebP 不存在标识
    JFIFColor = [System.Drawing.Color]::FromArgb(255, 0, 0)     # 红色 - JFIF 格式
    JPEGColor = [System.Drawing.Color]::FromArgb(0, 255, 0)     # 绿色 - JPEG 格式
    JPGColor = [System.Drawing.Color]::FromArgb(0, 0, 255)     # 蓝色 - JPG 格式
    PNGColor = [System.Drawing.Color]::FromArgb(255, 255, 0)   # 黄色 - PNG 格式
}

# 图片格式配置 - 定义各种图片格式的属性（扩展名、回退链、图像格式等）
$ImageFormatConfigs = @(
    @{
        Ext = 'webp'
        EdgeColor = $ImageBlockColors.WithWebPColor
        FallbackChain = @('webp')
        ImageFormat = [System.Drawing.Imaging.ImageFormat]::Webp
        GenerateImage = $false
        GenerateTestCases = $false
        ResponseBinaryFormat = 'WEBP'
        IsTestRequestExt = $true
    },
    @{
        Ext = 'png'
        EdgeColor = $ImageBlockColors.PNGColor
        FallbackChain = @('png')
        ImageFormat = [System.Drawing.Imaging.ImageFormat]::Png
        GenerateImage = $true
        GenerateTestCases = $true
        ResponseBinaryFormat = 'PNG'
        IsTestRequestExt = $true
    },
    @{
        Ext = 'jpg'
        EdgeColor = $ImageBlockColors.JPGColor
        FallbackChain = @('png', 'jpg')
        ImageFormat = [System.Drawing.Imaging.ImageFormat]::Jpeg
        GenerateImage = $true
        GenerateTestCases = $true
        ResponseBinaryFormat = 'JPG'
        IsTestRequestExt = $true
    },
    @{
        Ext = 'jpeg'
        EdgeColor = $ImageBlockColors.JPEGColor
        FallbackChain = @('png', 'jpeg')
        ImageFormat = [System.Drawing.Imaging.ImageFormat]::Jpeg
        GenerateImage = $true
        GenerateTestCases = $true
        ResponseBinaryFormat = 'JPG'
        IsTestRequestExt = $true
    },
    @{
        Ext = 'jfif'
        EdgeColor = $ImageBlockColors.JFIFColor
        FallbackChain = @('png', 'jfif')
        ImageFormat = [System.Drawing.Imaging.ImageFormat]::Jpeg
        GenerateImage = $true
        GenerateTestCases = $true
        ResponseBinaryFormat = 'JPG'
        IsTestRequestExt = $true
    }
)

# WebP 存在性配置
$WebPExistence = @(
    @{
        Suffix = 'with_webp'
        CornerColor = $ImageBlockColors.WithWebPColor
    },
    @{
        Suffix = 'no_webp'
        CornerColor = $ImageBlockColors.NoWebPColor
    }
)

# UseWebpHeader 选项 - 定义是否使用 Accept: image/webp header
$UseWebpHeaderOptions = @(
    @{
        Value = 1      # 使用 Accept: image/webp header
        Label = 'with_header'
    },
    @{
        Value = 0      # 不使用 Accept: image/webp header
        Label = 'no_header'
    }
)