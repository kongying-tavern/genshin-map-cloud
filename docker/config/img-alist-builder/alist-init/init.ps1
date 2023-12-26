#!/usr/bin/env pwsh

$BASE_DIR = "/data";
$DATA_DIR = -Join ($BASE_DIR, "/record");
$LOCK_FILE = -Join ($DATA_DIR, "/initialized.lock");

$Global:ALIST_TOKEN = $null;

# Utility functions
function Check-Lock {
    $hasLock = Test-Path -Path "${LOCK_FILE}";
    return $hasLock;
}

function Pass-Lock {
    $hasLock = Check-Lock;
    if($hasLock) {
        Write-Warning "Lock file exists, exiting ...";
        Exit;
    }
}

function Save-Lock {
    Set-Content "${LOCK_FILE}" -Value "" -Encoding UTF8 -Force;
}

function Fetch-Api {
    param(
        [Parameter(
            Mandatory = $true,
            ValueFromPipeline = $true,
            ValueFromPipelineByPropertyName
        )]
        [Microsoft.PowerShell.Commands.WebRequestMethod] $Method,

        [Parameter(
            Mandatory = $true,
            ValueFromPipeline = $true,
            ValueFromPipelineByPropertyName = $true
        )]
        [String] $Url,


        [Parameter(
            Mandatory = $false,
            ValueFromPipelineByPropertyName = $true
        )]
        [Object] $Params,

        [Parameter(
            Mandatory = $false,
            ValueFromPipelineByPropertyName = $true
        )]
        [Object] $Body
    )

    begin {
        $apiUri = -Join ("http://alist.local:5244/", $Url);
        $authHeaders = $Global:ALIST_TOKEN -eq $null ? @{} : @{ Authorization = $Global:ALIST_TOKEN };
        $bodyData = $Body -eq $null ? @{} : $Body;
        $bodyContent = ConvertTo-Json $bodyData;
    }

    process {
        $response = Invoke-RestMethod `
            -Method $Method `
            -Uri $apiUri `
            -Headers $authHeaders `
            -ContentType "application/json" `
            -Body $bodyContent;
        if($response -isnot [System.Management.Automation.PSObject]) {
            return @{};
        }
        return $response;
    }
}

function Get-SHA256 {
    param(
        [Parameter(
            Mandatory = $true,
            ValueFromPipeline = $true,
            ValueFromPipelineByPropertyName = $true
        )]
        [String] $Source
    )

    process {
        $srcBytes = [System.Text.Encoding]::UTF8.GetBytes($Source);
        $hash = [System.Security.Cryptography.SHA256]::Create().ComputeHash($srcBytes);
        $HashCode = [System.BitConverter]::ToString($hash).Replace("-", "").ToLower();
        return $HashCode;
    }
}

function Encrypt-Password {
    param(
        [Parameter(
            Mandatory = $true,
            ValueFromPipeline = $true,
            ValueFromPipelineByPropertyName = $true
        )]
        [String] $Password
    )

    process {
        $passwordSalted = -Join ($Password, "-https://github.com/alist-org/alist");
        $passwordEnc = Get-SHA256 $passwordSalted;
        return $passwordEnc;
    }
}

function ConvertTo-Integer {
    param(
        [Parameter(
            Mandatory = $true,
            ValueFromPipeline = $true,
            ValueFromPipelineByPropertyName = $true
        )]
        [Object] $NumericString
    )

    process {
        $numberStr = "{0}" -f $NumericString;
        $number = [int] $numberStr;
        return $number;
    }
}

# Process steps
function Step-Login {
    $loginRes = Fetch-Api Post "api/auth/login/hash" -Body @{
        username = "admin"
        password = Encrypt-Password ${Env:ALIST_ROOT_PASSWORD}
    };
    $Global:ALIST_TOKEN = $loginRes.data.token;
}

function Step-AddUser {
    Fetch-Api Post "api/admin/user/create" -Body @{
        username = ${Env:ALIST_USERNAME}
        password = ${Env:ALIST_PASSWORD}
        base_path = "/"
        role = 0
        permission = 0b0011111000
        disabled = $false
        sso_id = ""
    };
}

function Step-AddStorage {
    Fetch-Api Post "api/admin/storage/create" -Body @{
        mount_path = ${Env:ALIST_MOUNT_PATH}
        order = 0
        remark = "Image MinIO Storage"
        cache_expiration = ConvertTo-Integer ${Env:ALIST_CACHE_EXPIRE}
        web_proxy = $false
        webdav_policy = "302_redirect"
        down_proxy_url = ""
        extract_folder = ""
        enable_sign = $false
        driver = "S3"
        order_by = "name"
        order_direction = "asc"
        addition = ConvertTo-Json @{
            root_folder_path = "/"
            bucket = ${Env:MINIO_BUCKET_IMAGE}
            endpoint = "http://minio-proxy.local:80"
            region = "minio"
            access_key_id = ${Env:MINIO_KEY}
            secret_access_key = ${Env:MINIO_SECRET}
            session_token = ""
            custom_host = "minio-proxy.local:80"
            sign_url_expire = 4
            placeholder = ""
            force_path_style = $true
            list_object_version = "v1"
            remove_bucket = $false
            add_filename_to_disposition = $true
        }
    };
}

# Main process
Pass-Lock;
Step-Login;
Step-AddUser;
Step-AddStorage;
Save-Lock;
