$ErrorActionPreference = "Stop"

$RepoRoot = Resolve-Path (Join-Path $PSScriptRoot "..")
Push-Location $RepoRoot

try {
    Write-Host "== Kotlin / platform verification =="
    gradle --no-daemon :shared:compileKotlinDesktop --stacktrace
    gradle --no-daemon :shared:allTests --stacktrace
    gradle --no-daemon :androidApp:assembleDebug --stacktrace
    gradle --no-daemon :androidApp:lintDebug --stacktrace
    gradle --no-daemon :desktopApp:classes --stacktrace

    $PythonCommand = $null
    $PythonArgsPrefix = @()
    if (Get-Command python -ErrorAction SilentlyContinue) {
        $PythonCommand = "python"
    }
    elseif (Get-Command py -ErrorAction SilentlyContinue) {
        $PythonCommand = "py"
        $PythonArgsPrefix = @("-3")
    }
    else {
        throw "Python 3 is required for documentation and committed-secret checks."
    }

    Write-Host "== Documentation verification =="
    & $PythonCommand @PythonArgsPrefix scripts/check_docs_links.py
    if ($LASTEXITCODE -ne 0) { throw "Documentation verification failed." }

    Write-Host "== Committed-secret verification =="
    & $PythonCommand @PythonArgsPrefix scripts/check_for_secrets.py
    if ($LASTEXITCODE -ne 0) { throw "Committed-secret verification failed." }

    if (Get-Command cargo -ErrorAction SilentlyContinue) {
        Write-Host "== Optional Rust verification =="
        Push-Location rust-engine
        try {
            cargo fmt --all -- --check
            if ($LASTEXITCODE -ne 0) { throw "Rust formatting verification failed." }
            cargo clippy --all-targets --all-features -- -D warnings
            if ($LASTEXITCODE -ne 0) { throw "Rust Clippy verification failed." }
            cargo test --all-targets --all-features
            if ($LASTEXITCODE -ne 0) { throw "Rust tests failed." }
        }
        finally {
            Pop-Location
        }
    }
    else {
        Write-Host "cargo not installed; skipping optional Rust verification"
    }

    Write-Host "RPS Arena local verification completed successfully."
}
finally {
    Pop-Location
}
