param(
    [Parameter(Mandatory = $true)]
    [string]$InputZip,

    [string]$OutputZip
)

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

if (-not (Test-Path -LiteralPath $InputZip)) {
    throw "Input zip not found: $InputZip"
}

if ([string]::IsNullOrWhiteSpace($OutputZip)) {
    $base = [System.IO.Path]::GetFileNameWithoutExtension($InputZip)
    $dir = [System.IO.Path]::GetDirectoryName($InputZip)
    if ([string]::IsNullOrWhiteSpace($dir)) {
        $dir = '.'
    }
    $OutputZip = Join-Path $dir ($base + '-fixed.zip')
}

$workRoot = Join-Path ([System.IO.Path]::GetTempPath()) ("sim-backup-fix-" + [Guid]::NewGuid().ToString('N'))
$extractDir = Join-Path $workRoot 'src'

try {
    New-Item -ItemType Directory -Path $extractDir -Force | Out-Null
    Expand-Archive -LiteralPath $InputZip -DestinationPath $extractDir -Force

    $targetCsv = Join-Path $extractDir 'tables\dashboard_daily_summary.csv'
    if (-not (Test-Path -LiteralPath $targetCsv)) {
        throw "Expected CSV not found in backup: tables/dashboard_daily_summary.csv"
    }

    $rows = Import-Csv -LiteralPath $targetCsv
    foreach ($row in $rows) {
        if ($null -ne $row.summary_day -and $row.summary_day -match '^(\d{4}-\d{2}-\d{2})T') {
            $row.summary_day = $Matches[1]
        }
    }

    $csvLines = $rows | ConvertTo-Csv -NoTypeInformation
    $utf8NoBom = New-Object System.Text.UTF8Encoding($false)
    [System.IO.File]::WriteAllLines($targetCsv, $csvLines, $utf8NoBom)

    if (Test-Path -LiteralPath $OutputZip) {
        Remove-Item -LiteralPath $OutputZip -Force
    }

    Compress-Archive -Path (Join-Path $extractDir '*') -DestinationPath $OutputZip -CompressionLevel Optimal
    Write-Host "Created fixed backup zip: $OutputZip"
}
finally {
    if (Test-Path -LiteralPath $workRoot) {
        Remove-Item -LiteralPath $workRoot -Recurse -Force
    }
}
